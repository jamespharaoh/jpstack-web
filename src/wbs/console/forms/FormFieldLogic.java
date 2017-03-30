package wbs.console.forms;

import static wbs.utils.collection.CollectionUtils.collectionIsEmpty;
import static wbs.utils.collection.IterableUtils.iterableMapToList;
import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.collection.MapUtils.mapItemForKey;
import static wbs.utils.etc.Misc.doNothing;
import static wbs.utils.etc.NumberUtils.equalToOne;
import static wbs.utils.etc.NumberUtils.equalToThree;
import static wbs.utils.etc.NumberUtils.equalToTwo;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.moreThanZero;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang3.tuple.Pair;

import wbs.console.forms.FormField.UpdateResult;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.PermanentRecord;
import wbs.framework.exception.DetailedException;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.utils.string.FormatWriter;

@SingletonComponent ("fieldsLogic")
@SuppressWarnings ({ "rawtypes", "unchecked" })
public
class FormFieldLogic {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	UserPrivChecker privChecker;

	// public implementation

	public <Container>
	void implicit (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormFieldSet <Container> formFieldSet,
			@NonNull Container container) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"implicit");

		for (
			FormField <Container, ?, ?, ?> formField
				: formFieldSet.formFields ()
		) {

			formField.implicit (
				taskLogger,
				container);

		}

	}

	public
	UpdateResultSet update (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ConsoleRequestContext requestContext,
			@NonNull FormFieldSet formFieldSet,
			@NonNull Object container,
			@NonNull Map <String, Object> hints,
			@NonNull String formName) {

		UpdateResultSet updateResultSet =
			new UpdateResultSet ();

		update (
			parentTaskLogger,
			requestContext,
			formFieldSet,
			updateResultSet,
			container,
			hints,
			formName);

		return updateResultSet;

	}

	public
	void update (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ConsoleRequestContext requestContext,
			@NonNull FormFieldSet formFieldSet,
			@NonNull UpdateResultSet updateResults,
			@NonNull Object container,
			@NonNull Map <String, Object> hints,
			@NonNull String formName) {

		update (
			parentTaskLogger,
			requestContextToSubmission (
				requestContext),
			formFieldSet,
			updateResults,
			container,
			hints,
			formName);

	}

	public <Container>
	void update (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormFieldSubmission submission,
			@NonNull FormFieldSet <Container> formFieldSet,
			@NonNull UpdateResultSet updateResults,
			@NonNull Container container,
			@NonNull Map <String,Object> hints,
			@NonNull String formName) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"update");

		for (
			FormField <Container, ?, ?, ?> formField
				: formFieldSet.formFields ()
		) {

			UpdateResult updateResult;

			try {

				updateResult =
					formField.update (
						taskLogger,
						submission,
						container,
						hints,
						formName);

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

				updateResults.errorCount ++;

			} else if (
				updateResult.updated ()
			) {

				updateResults.updateCount ++;

			}

			updateResults.updateResults ().put (
				Pair.of (
					formName,
					formField.name ()),
				updateResult);

		}

	}

	public
	void reportErrors (
			@NonNull ConsoleRequestContext requestContext,
			@NonNull UpdateResultSet updateResultSet,
			@NonNull String formName) {

		List <String> errorFieldNames =
			new ArrayList<> ();

		for (
			Map.Entry <
				Pair <String, String>,
				UpdateResult <?, ?>
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

			UpdateResult updateResult =
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

	public
	void runUpdateHooks (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormFieldSet formFieldSet,
			@NonNull UpdateResultSet updateResultSet,
			@NonNull Object container,
			@NonNull PermanentRecord <?> linkObject,
			@NonNull Optional <Object> objectRef,
			@NonNull Optional <String> objectType,
			@NonNull String formName) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"runUpdateHooks");

		for (
			Map.Entry <Pair <String, String>, UpdateResult <?, ?>>
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

			FormField formField =
				formFieldSet.formField (
					updateResultEntry.getKey ().getRight ());

			UpdateResult updateResult =
				(UpdateResult)
				updateResultEntry.getValue ();

			if (! updateResult.updated ())
				continue;

			formField.runUpdateHook (
				taskLogger,
				updateResult,
				container,
				linkObject,
				objectRef,
				objectType);

		}

	}

	public <Container>
	void outputTableHeadings (
			@NonNull FormatWriter htmlWriter,
			@NonNull FormFieldSet <Container> formFieldSet) {

		for (
			FormField <Container, ?, ?, ?> formField
				: formFieldSet.formFields ()
		) {

			htmlWriter.writeLineFormat (
				"<th>%h</th>",
				formField.label ());

		}

	}

	public <Container>
	void outputCsvHeadings (
			@NonNull FormatWriter csvWriter,
			@NonNull List <FormFieldSet <Container>> formFieldSets) {

		boolean first =
			true;

		for (
			FormFieldSet <Container> formFieldSet
				: formFieldSets
		) {

			for (
				FormField formField
					: formFieldSet.formFields ()
			) {

				if (! first) {

					csvWriter.writeFormat (
						",");

				}

				csvWriter.writeFormat (
					"\"%s\"",
					stringReplaceAllSimple (
						"\"",
						"\"\"",
						formField.label ()));

				first = false;

			}

		}

		csvWriter.writeFormat (
			"\n");

	}

	public
	FormFieldSubmission requestContextToSubmission (
			@NonNull ConsoleRequestContext requestContext) {

		return new FormFieldSubmissionImplementation ()

			.multipart (
				requestContext.isMultipart ())

			.parameters (
				requestContext.formData ())

			.fileItems (
				requestContext.isMultipart ()
					? Maps.uniqueIndex (
						requestContext.fileItems (),
						FileItem::getFieldName)
					: null);

	}

	public <Container>
	void outputFormRows (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ConsoleRequestContext requestContext,
			@NonNull FormatWriter htmlWriter,
			@NonNull FormFieldSet <Container> formFieldSet,
			@NonNull Optional <UpdateResultSet> updateResultSet,
			@NonNull Container object,
			@NonNull Map <String, Object> hints,
			@NonNull FormType formType,
			@NonNull String formName) {

		outputFormRows (
			parentTaskLogger,
			requestContextToSubmission (
				requestContext),
			htmlWriter,
			formFieldSet,
			updateResultSet,
			object,
			hints,
			formType,
			formName);

	}

	public <Container>
	void outputFormAlwaysHidden (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ConsoleRequestContext requestContext,
			@NonNull FormatWriter htmlWriter,
			@NonNull FormFieldSet <Container> formFieldSet,
			@NonNull Optional <UpdateResultSet> updateResultSet,
			@NonNull Container object,
			@NonNull Map <String, Object> hints,
			@NonNull FormType formType,
			@NonNull String formName) {

		outputFormAlwaysHidden (
			parentTaskLogger,
			requestContextToSubmission (
				requestContext),
			htmlWriter,
			formFieldSet,
			updateResultSet,
			object,
			hints,
			formType,
			formName);

	}

	public <Container>
	void outputFormAlwaysHidden (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter htmlWriter,
			@NonNull FormFieldSet <Container> formFieldSet,
			@NonNull Optional <UpdateResultSet> updateResultSet,
			@NonNull Container object,
			@NonNull Map <String, Object> hints,
			@NonNull FormType formType,
			@NonNull String formName) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"outputFormAlwaysHidden");

		for (
			FormField <Container, ?, ?, ?> formField
				: formFieldSet.formFields ()
		) {

			if (
				! formField.canView (
					object,
					hints)
			) {
				continue;
			}

			if (
				optionalIsPresent (
					updateResultSet)
			) {

				updateResultSet.get ().updateResults ().get (
					stringFormat (
						"%s-%s",
						formName,
						formField.name ()));

			}

			formField.renderFormAlwaysHidden (
				taskLogger,
				submission,
				htmlWriter,
				object,
				hints,
				formType,
				formName);

		}

	}

	public
	void outputFormTemporarilyHidden (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ConsoleRequestContext requestContext,
			@NonNull FormatWriter htmlWriter,
			@NonNull FormFieldSet formFieldSet,
			@NonNull Object object,
			@NonNull Map<String,Object> hints,
			@NonNull FormType formType,
			@NonNull String formName) {

		outputFormTemporarilyHidden (
			parentTaskLogger,
			requestContextToSubmission (
				requestContext),
			htmlWriter,
			formFieldSet,
			object,
			hints,
			formType,
			formName);

	}

	public <Container>
	void outputFormTemporarilyHidden (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter htmlWriter,
			@NonNull FormFieldSet <Container> formFieldSet,
			@NonNull Container object,
			@NonNull Map <String, Object> hints,
			@NonNull FormType formType,
			@NonNull String formName) {

		for (
			FormField <Container, ?, ?, ?> formField
				: formFieldSet.formFields ()
		) {

			if (
				! formField.canView (
					object,
					hints)
			) {
				continue;
			}

			formField.renderFormTemporarilyHidden (
				parentTaskLogger,
				submission,
				htmlWriter,
				object,
				hints,
				formType,
				formName);

		}

	}

	public <Container>
	void outputFormRows (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter htmlWriter,
			@NonNull FormFieldSet <Container> formFieldSet,
			@NonNull Optional <UpdateResultSet> updateResultSet,
			@NonNull Container object,
			@NonNull Map <String, Object> hints,
			@NonNull FormType formType,
			@NonNull String formName) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"outputFormRows");

		for (
			FormField formField
				: formFieldSet.formFields ()
		) {

			if (
				! formField.canView (
					object,
					hints)
			) {
				continue;
			}

			Optional <String> error;

			if (
				optionalIsPresent (
					updateResultSet)
			) {

				Optional <UpdateResult <?, ?>> updateResultOptional =
					mapItemForKey (
						updateResultSet.get ().updateResults (),
						Pair.of (
							formName,
							formField.name ()));

				if (
					optionalIsNotPresent (
						updateResultOptional)
				) {

					taskLogger.errorFormat (
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
					taskLogger,
					submission,
					htmlWriter,
					object,
					hints,
					error,
					formType,
					formName);

			} catch (Exception exception) {

				throw new DetailedException (
					stringFormat (
						"Error rendering field %s",
						formField.name ()),
					exception,
					exceptionDetails (
						hints));

			}

		}

	}

	public <Container>
	void outputFormReset (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormatWriter javascriptWriter,
			@NonNull FormFieldSet <Container> formFieldSet,
			@NonNull FormType formType,
			@NonNull Container object,
			@NonNull Map <String, Object> hints,
			@NonNull String formName) {

		for (
			FormField <Container, ?, ?, ?> formField
				: formFieldSet.formFields ()
		) {

			formField.renderFormReset (
				parentTaskLogger,
				javascriptWriter,
				object,
				hints,
				formType,
				formName);

		}

	}

	public
	void outputFormTable (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ConsoleRequestContext requestContext,
			@NonNull FormatWriter htmlWriter,
			@NonNull FormFieldSet formFieldSet,
			@NonNull Optional <UpdateResultSet> updateResultSet,
			@NonNull Object object,
			@NonNull Map <String, Object> hints,
			@NonNull String method,
			@NonNull String actionUrl,
			@NonNull String submitButtonLabel,
			@NonNull FormType formType,
			@NonNull String formName) {

		outputFormTable (
			parentTaskLogger,
			requestContextToSubmission (
				requestContext),
			htmlWriter,
			formFieldSet,
			updateResultSet,
			object,
			hints,
			method,
			actionUrl,
			submitButtonLabel,
			formType,
			formName);

	}

	public
	void outputFormTable (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter formatWriter,
			@NonNull FormFieldSet formFieldSet,
			@NonNull Optional <UpdateResultSet> updateResultSet,
			@NonNull Object object,
			@NonNull Map <String, Object> hints,
			@NonNull String method,
			@NonNull String actionUrl,
			@NonNull String submitButtonLabel,
			@NonNull FormType formType,
			@NonNull String formName) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"outputFormTable");

		outputFormDebug (
			taskLogger,
			formatWriter,
			formFieldSet,
			object,
			hints);

		String enctype = (
			(BooleanSupplier)
			() -> {
				try {
					return formFieldSet.fileUpload ();
				} catch (Exception exception) {
					return false;
				}
			}
		).getAsBoolean ()
			? "multipart/form-data"
			: "application/x-www-form-urlencoded";

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
			taskLogger,
			submission,
			formatWriter,
			formFieldSet,
			updateResultSet,
			object,
			hints,
			formType,
			formName);

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

		htmlParagraphClose ();

		htmlFormClose ();

	}

	public
	void outputDetailsTable (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormatWriter htmlWriter,
			@NonNull FormFieldSet formFieldSet,
			@NonNull Object object,
			@NonNull Map<String,Object> hints) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"outputDetailsTable");

		htmlTableOpenDetails (
			htmlWriter);

		outputTableRows (
			taskLogger,
			htmlWriter,
			formFieldSet,
			object,
			hints);

		htmlTableClose ();

	}

	public <Container>
	void outputListTable (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormatWriter htmlWriter,
			@NonNull FormFieldSet <Container> formFieldSet,
			@NonNull List <Container> objects,
			@NonNull Boolean links) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"outputListTable");

		// table open

		htmlTableOpenList (
			htmlWriter);

		// table header

		htmlTableRowOpen (
			htmlWriter);

		outputTableHeadings (
			htmlWriter,
			formFieldSet);

		htmlTableRowClose (
			htmlWriter);

		// table content

		if (
			collectionIsEmpty (
				objects)
		) {

			htmlTableRowOpen (
				htmlWriter);

			htmlTableCellWrite (
				htmlWriter,
				"There is no data to display",
				htmlColumnSpanAttribute (
					formFieldSet.columns ()));

			htmlTableRowClose (
				htmlWriter);

		} else {

			for (
				Container object
					: objects
			) {

				htmlTableRowOpen (
					htmlWriter);

				outputTableCellsList (
					taskLogger,
					htmlWriter,
					formFieldSet,
					object,
					emptyMap (),
					links);

				htmlTableRowClose (
					htmlWriter);

			}

		}

		// table close

		htmlTableClose (
			htmlWriter);

	}

	public <Container>
	void outputCsvRow (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormatWriter csvWriter,
			@NonNull List <FormFieldSet <Container>> formFieldSets,
			@NonNull Container object,
			@NonNull Map <String, Object> hints) {

		boolean first = true;

		for (
			FormFieldSet <Container> formFieldSet
				: formFieldSets
		) {

			for (
				FormField <Container, ?, ?, ?> formField
					: formFieldSet.formFields ()
			) {

				if (! first) {

					csvWriter.writeFormat (
						",");

				}

				formField.renderCsvRow (
					parentTaskLogger,
					csvWriter,
					object,
					hints);

				first = false;

			}

		}

		csvWriter.writeFormat (
			"\n");

	}

	public <Container>
	void outputTableCellsList (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormatWriter htmlWriter,
			@NonNull FormFieldSet <Container> formFieldSet,
			@NonNull Container object,
			@NonNull Map <String, Object> hints,
			@NonNull Boolean links) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"outputTableCellsList");

		for (
			FormField <Container, ?, ?, ?> formField
				: formFieldSet.formFields ()
		) {

			try {

				formField.renderTableCellList (
					taskLogger,
					htmlWriter,
					object,
					hints,
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

	public <Container>
	void outputTableRowsList (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormatWriter htmlWriter,
			@NonNull FormFieldSet <Container> formFieldSet,
			@NonNull Container object,
			@NonNull Boolean links,
			@NonNull Long columnSpan) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"outputTableRowsList");

		for (
			FormField <Container, ?, ?, ?> formField
				: formFieldSet.formFields ()
		) {

			formField.renderTableCellList (
				taskLogger,
				htmlWriter,
				object,
				ImmutableMap.of (),
				links,
				columnSpan);

		}

	}

	public <Container>
	void outputTableRows (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormatWriter htmlWriter,
			@NonNull FormFieldSet <Container> formFieldSet,
			@NonNull Container object,
			@NonNull Map <String, Object> hints) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"outputTableRows");

		for (
			FormField <Container, ?, ?, ?> formField
				: formFieldSet.formFields ()
		) {

			if (
				! formField.canView (
					object,
					hints)
			) {
				continue;
			}

			htmlTableRowOpen (
				htmlWriter);

			htmlTableHeaderCellWrite (
				htmlWriter,
				formField.label ());

			formField.renderTableCellProperties (
				taskLogger,
				htmlWriter,
				object,
				hints);

			htmlTableRowClose (
				htmlWriter);

		}

	}

	public <Container>
	void outputFormDebug (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormatWriter formatWriter,
			@NonNull FormFieldSet formFieldSet,
			@NonNull Container object,
			@NonNull Map <String, Object> hints) {

		if (
			! privChecker.canSimple (
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

	public
	Map <String, List <String>> exceptionDetails (
			@NonNull Map <String, Object> hints) {

		return ImmutableMap.<String, List <String>> builder ()

			.put (
				"Form hints",
				iterableMapToList (
					hintEntry ->
						stringFormat (
							"%s = %s",
							hintEntry.getKey (),
							hintEntry.getValue ().toString ()),
					hints.entrySet ()))

			.build ();

	}

	@Accessors (fluent = true)
	@Data
	public static
	class UpdateResultSet {

		Map <Pair <String, String>, UpdateResult <?, ?>> updateResults =
			new LinkedHashMap<> ();

		long errorCount;
		long updateCount;

		public
		boolean errors () {

			return moreThanZero (
				errorCount);

		}

	}

}
