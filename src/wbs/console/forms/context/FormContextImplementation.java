package wbs.console.forms.context;

import static wbs.utils.collection.CollectionUtils.collectionIsEmpty;
import static wbs.utils.collection.IterableUtils.iterableMapToList;
import static wbs.utils.collection.MapUtils.mapItemForKey;
import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.Misc.doNothing;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.NullUtils.filterNotNull;
import static wbs.utils.etc.NumberUtils.equalToOne;
import static wbs.utils.etc.NumberUtils.equalToThree;
import static wbs.utils.etc.NumberUtils.equalToTwo;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.objectToString;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringNotEqualSafe;
import static wbs.utils.string.StringUtils.stringReplaceAllSimple;
import static wbs.web.utils.HtmlAttributeUtils.htmlColumnSpanAttribute;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenMethodActionEncoding;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenList;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.tuple.Pair;

import wbs.console.forms.core.CombinedFormFieldSet;
import wbs.console.forms.core.FormFieldSet;
import wbs.console.forms.types.FormField;
import wbs.console.forms.types.FormFieldSubmission;
import wbs.console.forms.types.FormType;
import wbs.console.forms.types.FormUpdateResult;
import wbs.console.forms.types.FormUpdateResultSet;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.PermanentRecord;
import wbs.framework.exception.DetailedException;
import wbs.framework.logging.LogContext;

import wbs.utils.string.FormatWriter;

@PrototypeComponent ("formContext")
@Accessors (fluent = true)
public
class FormContextImplementation <Container>
	implements FormContext <Container> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// properties

	@Getter @Setter
	ConsoleRequestContext requestContext;

	@Getter @Setter
	UserPrivChecker privChecker;

	@Getter @Setter
	FormFieldSet <Container> columnFields;

	@Getter @Setter
	FormFieldSet <Container> rowFields;

	@Getter @Setter
	String formName;

	@Getter @Setter
	FormType formType;

	@Getter @Setter
	Class <Container> containerClass;

	@Getter @Setter
	List <Container> objects;

	@Getter @Setter
	Map <String, Object> hints;

	@Getter @Setter
	FormFieldSubmission submission;

	@Getter @Setter
	FormatWriter formatWriter;

	@Getter @Setter
	FormUpdateResultSet updateResultSet;

	// accesors

	@Override
	public
	FormFieldSet <Container> allFields () {

		return new CombinedFormFieldSet <Container> (
			containerClass,
			filterNotNull (
				columnFields,
				rowFields));

	}

	// implementation

	@Override
	public
	void implicit (
			@NonNull Transaction parentTransaction,
			@NonNull Container object) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"implicit");

		) {

			for (
				FormField <Container, ?, ?, ?> formField
					: allFields ().formFields ()
			) {

				formField.implicit (
					transaction,
					this,
					object);

			}

		}

	}

	@Override
	public
	void setDefaults (
			@NonNull Transaction parentTransaction,
			@NonNull Container object) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"setDefaults");

		) {

			for (
				FormField <Container, ?, ?, ?> formField
					: allFields ().formFields ()
			) {

				formField.setDefault (
					transaction,
					this,
					object);

			}

		}

	}

	@Override
	public
	void update (
			@NonNull Transaction parentTransaction,
			@NonNull Container object) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"update");

		) {

			for (
				FormField <Container, ?, ?, ?> formField
					: allFields ().formFields ()
			) {

				FormUpdateResult <?, ?> updateResult;

				try {

					updateResult =
						formField.update (
							transaction,
							this,
							object);

				} catch (Exception exception) {

					throw new RuntimeException (
						stringFormat (
							"Error updating field %s",
							formField.name ()),
						exception);

				}

				if (
					optionalIsPresent (
						updateResult.error ())
				) {

					updateResultSet.errorCount (
						updateResultSet.errorCount () + 1);

				} else if (
					updateResult.updated ()
				) {

					updateResultSet.updateCount (
						updateResultSet.updateCount () + 1);

				}

				updateResultSet.updateResults ().put (
					Pair.of (
						formName,
						formField.name ()),
					updateResult);

			}

		}

	}

	@Override
	public
	void reportErrors (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"reportErrors");

		) {

			List <String> errorFieldNames =
				new ArrayList<> ();

			for (
				Map.Entry <
					Pair <String, String>,
					FormUpdateResult <?, ?>
				> updateResultEntry
					: updateResultSet.updateResults ().entrySet ()
			) {

				if (
					stringNotEqualSafe (
						updateResultEntry.getKey ().getLeft (),
						formName)
				) {
					continue;
				}

				String formFieldName =
					updateResultEntry.getKey ().getRight ();

				FormUpdateResult <?, ?> updateResult =
					updateResultEntry.getValue ();

				if (
					optionalIsPresent (
						updateResult.error ())
				) {

					errorFieldNames.add (
						formFieldName);

				}

			}

			if (
				collectionIsEmpty (
					errorFieldNames)
			) {

				doNothing ();

			} else if (
				equalToOne (
					errorFieldNames.size ())
			) {

				requestContext.addError (
					stringFormat (
						"The '%s' field is not valid",
						errorFieldNames.get (0)));

			} else if (
				equalToTwo (
					errorFieldNames.size ())
			) {

				requestContext.addError (
					stringFormat (
						"The '%s' and '%s' fields are invalid",
						errorFieldNames.get (0),
						errorFieldNames.get (1)));

			} else if (
				equalToThree (
					errorFieldNames.size ())
			) {

				requestContext.addError (
					stringFormat (
						"The '%s', '%s' and '%s' fields are invalid",
						errorFieldNames.get (0),
						errorFieldNames.get (1),
						errorFieldNames.get (2)));

			} else {

				requestContext.addError (
					stringFormat (
						"There are %s invalid fields",
						integerToDecimalString (
							errorFieldNames.size ())));

			}

		}

	}

	@Override
	public
	void runUpdateHooks (
			@NonNull Transaction parentTransaction,
			@NonNull Container object,
			@NonNull PermanentRecord <?> linkObject,
			@NonNull Optional <Object> objectRef,
			@NonNull Optional <String> objectType) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"runUpdateHooks");

		) {

			for (
				Map.Entry <Pair <String, String>, FormUpdateResult <?, ?>>
				updateResultEntry
					: updateResultSet.updateResults ().entrySet ()
			) {

				if (
					stringNotEqualSafe (
						updateResultEntry.getKey ().getLeft (),
						formName)
				) {
					continue;
				}

				FormField <Container, ?, ?, ?> formField =
					allFields ().formField (
						updateResultEntry.getKey ().getRight ());

				FormUpdateResult <?, ?> updateResult =
					updateResultEntry.getValue ();

				if (! updateResult.updated ())
					continue;

				formField.runUpdateHook (
					transaction,
					this,
					object,
					genericCastUnchecked (
						updateResult),
					linkObject,
					objectRef,
					objectType);

			}

		}

	}

	@Override
	public
	void outputTableHeadings (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"outputTableHeadings");

		) {

			for (
				FormField <Container, ?, ?, ?> formField
					: columnFields ().formFields ()
			) {

				formatWriter.writeLineFormat (
					"<th>%h</th>",
					formField.label ());

			}

		}

	}

	@Override
	public
	void outputCsvHeadings (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"outputCsvHeadings");

		) {

			boolean first =
				true;

			for (
				FormField <Container, ?, ?, ?> formField
					: allFields ().formFields ()
			) {

				if (! first) {

					formatWriter.writeFormat (
						",");

				}

				formatWriter.writeFormat (
					"\"%s\"",
					stringReplaceAllSimple (
						"\"",
						"\"\"",
						formField.label ()));

				first = false;

			}

			formatWriter.writeFormat (
				"\n");

		}

	}

	@Override
	public
	void outputFormAlwaysHidden (
			@NonNull Transaction parentTransaction,
			@NonNull Container object) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"outputFormAlwaysHidden");

		) {

			for (
				FormField <Container, ?, ?, ?> formField
					: allFields ().formFields ()
			) {

				if (
					! formField.canView (
						transaction,
						this,
						object)
				) {
					continue;
				}

				if (
					isNotNull (
						updateResultSet)
				) {

					updateResultSet.updateResults ().get (
						stringFormat (
							"%s-%s",
							formName,
							formField.name ()));

				}

				formField.renderFormAlwaysHidden (
					transaction,
					this,
					object);

			}

		}

	}

	@Override
	public
	void outputFormTemporarilyHidden (
			@NonNull Transaction parentTransaction,
			@NonNull Container object) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"outputFormTemporarilyHidden");

		) {

			for (
				FormField <Container, ?, ?, ?> formField
					: allFields ().formFields ()
			) {

				if (
					! formField.canView (
						transaction,
						this,
						object)
				) {
					continue;
				}

				formField.renderFormTemporarilyHidden (
					transaction,
					this,
					object);

			}

		}

	}

	@Override
	public
	void outputFormRows (
			@NonNull Transaction parentTransaction,
			@NonNull Container container) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"outputFormRows");

		) {

			for (
				FormField <Container, ?, ?, ?> formField
					: allFields ().formFields ()
			) {

				if (
					! formField.canView (
						transaction,
						this,
						container)
				) {
					continue;
				}

				Optional <String> error;

				if (
					isNotNull (
						updateResultSet)
				) {

					Optional <FormUpdateResult <?, ?>> updateResultOptional =
						mapItemForKey (
							updateResultSet.updateResults (),
							Pair.of (
								formName,
								formField.name ()));

					if (
						optionalIsNotPresent (
							updateResultOptional)
					) {

						transaction.errorFormat (
							"Unable to find update result for %s-%s",
							formName,
							formField.name ());

						error =
							optionalAbsent ();

					} else {

						error =
							updateResultOptional.get ().error ();

					}

				} else {

					error =
						optionalAbsent ();

				}

				try {

					formField.renderFormRow (
						transaction,
						this,
						container,
						error);

				} catch (Exception exception) {

					throw new DetailedException (
						stringFormat (
							"Error rendering field %s",
							formField.name ()),
						exception,
						exceptionDetails ());

				}

			}

		}

	}

	@Override
	public
	void outputFormReset (
			@NonNull Transaction parentTransaction,
			@NonNull Container container) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"outputFormReset");

		) {

			for (
				FormField <Container, ?, ?, ?> formField
					: allFields ().formFields ()
			) {

				formField.renderFormReset (
					transaction,
					this,
					container);

			}

		}

	}

	@Override
	public
	void outputFormTable (
			@NonNull Transaction parentTransaction,
			@NonNull Container object,
			@NonNull String method,
			@NonNull String actionUrl,
			@NonNull String submitButtonLabel) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"outputFormTable");

		) {

			outputFormDebug (
				transaction);

			String enctype =
				ifThenElse (
					allFields ().fileUpload (),
					() -> "multipart/form-data",
					() -> "application/x-www-form-urlencoded");

			htmlFormOpenMethodActionEncoding (
				formatWriter,
				method,
				actionUrl,
				enctype);

			formatWriter.writeLineFormat (
				"<input",
				" type=\"hidden\"",
				" name=\"form.name\"",
				" value=\"%h\"",
				formName,
				">");

			htmlTableOpenDetails (
				formatWriter);

			outputFormRows (
				transaction,
				object);

			htmlTableClose (
				formatWriter);

			htmlParagraphOpen (
				formatWriter);

			formatWriter.writeLineFormat (
				"<input",
				" type=\"submit\"",
				" value=\"%h\"",
				submitButtonLabel,
				">");

			htmlParagraphClose (
				formatWriter);

			htmlFormClose (
				formatWriter);

		}

	}

	@Override
	public
	void outputDetailsTable (
			@NonNull Transaction parentTransaction,
			@NonNull Container object) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"outputDetailsTable");

		) {

			htmlTableOpenDetails (
				formatWriter);

			outputTableRows (
				transaction,
				object);

			htmlTableClose (
				formatWriter);

		}

	}

	@Override
	public
	void outputListTable (
			@NonNull Transaction parentTransaction,
			@NonNull Boolean links) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"outputListTable");

		) {

			// table open

			htmlTableOpenList (
				formatWriter);

			// table header

			htmlTableRowOpen (
				formatWriter);

			outputTableHeadings (
				transaction);

			htmlTableRowClose (
				formatWriter);

			// table content

			if (
				collectionIsEmpty (
					objects)
			) {

				htmlTableRowOpen (
					formatWriter);

				htmlTableCellWrite (
					formatWriter,
					"There is no data to display",
					htmlColumnSpanAttribute (
						columnFields.columns ()));

				htmlTableRowClose (
					formatWriter);

			} else {

				for (
					Container object
						: objects
				) {

					htmlTableRowOpen (
						formatWriter);

					outputTableCellsList (
						transaction,
						object,
						links);

					htmlTableRowClose (
						formatWriter);

					if (
						isNotNull (
							rowFields)
					) {

						for (
							FormField <Container, ?, ?, ?> rowField
								: rowFields.formFields ()
						) {

							htmlTableRowOpen (
								formatWriter);

							try {

								rowField.renderTableCellList (
									transaction,
									this,
									object,
									links,
									columnFields.columns ());

							} catch (Exception exception) {

								throw new RuntimeException (
									stringFormat (
										"Error rendering field %s for %s",
										rowField.name (),
										objectToString (
											object)),
									exception);

							}

							htmlTableRowClose (
								formatWriter);

						}

					}

				}

			}

			// table close

			htmlTableClose (
				formatWriter);

		}

	}

	@Override
	public
	void outputCsvRow (
			@NonNull Transaction parentTransaction,
			@NonNull Container object) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"outputCsvRow");

		) {

			boolean first = true;

			for (
				FormField <Container, ?, ?, ?> formField
					: allFields ().formFields ()
			) {

				if (! first) {

					formatWriter.writeFormat (
						",");

				}

				formField.renderCsvRow (
					transaction,
					this,
					object);

				first = false;

			}

			formatWriter.writeFormat (
				"\n");

		}

	}

	@Override
	public
	void outputTableCellsList (
			@NonNull Transaction parentTransaction,
			@NonNull Container object,
			@NonNull Boolean links) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"outputTableCellsList");

		) {

			for (
				FormField <Container, ?, ?, ?> formField
					: columnFields.formFields ()
			) {

				try {

					formField.renderTableCellList (
						transaction,
						this,
						object,
						links,
						1l);

				} catch (Exception exception) {

					throw new RuntimeException (
						stringFormat (
							"Error rendering field %s for %s",
							formField.name (),
							objectToString (
								object)),
						exception);

				}

			}

		}

	}

	@Override
	public
	void outputTableRowsList (
			@NonNull Transaction parentTransaction,
			@NonNull Container object,
			@NonNull Boolean links,
			@NonNull Long columnSpan) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"outputTableRowsList");

		) {

			for (
				FormField <Container, ?, ?, ?> formField
					: allFields ().formFields ()
			) {

				formField.renderTableCellList (
					transaction,
					this,
					object,
					links,
					columnSpan);

			}

		}

	}

	@Override
	public
	void outputTableRows (
			@NonNull Transaction parentTransaction,
			@NonNull Container object) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"outputTableRows");

		) {

			for (
				FormField <Container, ?, ?, ?> formField
					: allFields ().formFields ()
			) {

				if (
					! formField.canView (
						transaction,
						this,
						object)
				) {
					continue;
				}

				htmlTableRowOpen (
					formatWriter);

				htmlTableHeaderCellWrite (
					formatWriter,
					formField.label ());

				formField.renderTableCellProperties (
					transaction,
					this,
					object,
					1l);

				htmlTableRowClose (
					formatWriter);

			}

		}

	}

	@Override
	public
	void outputFormDebug (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"outputFormDebug");

		) {

			if (
				! privChecker.canSimple (
					transaction,
					GlobalId.root,
					"debug")
			) {
				return;
			}

			// open comment

			formatWriter.writeLineFormatIncreaseIndent (
				"<!--");

			// hints

			formatWriter.writeNewline ();

			formatWriter.writeLineFormatIncreaseIndent (
				"Hints");

			formatWriter.writeNewline ();

			hints.entrySet ().forEach (
				hintEntry ->
					formatWriter.writeLineFormat (
						"%s = %s",
						hintEntry.getKey (),
						hintEntry.getValue ().toString ()));

			formatWriter.decreaseIndent ();

			// close comment

			formatWriter.writeNewline ();

			formatWriter.writeLineFormatDecreaseIndent (
				"-->");

		}

	}

	@Override
	public
	Map <String, List <String>> exceptionDetails () {

		return ImmutableMap.<String, List <String>> builder ()

			.put (
				"Form hints",
				iterableMapToList (
					hints.entrySet (),
					hintEntry ->
						stringFormat (
							"%s = %s",
							hintEntry.getKey (),
							hintEntry.getValue ().toString ())))

			.build ();

	}

}
