package wbs.console.forms;

import static wbs.utils.collection.CollectionUtils.collectionIsEmpty;
import static wbs.utils.collection.MapUtils.mapItemForKey;
import static wbs.utils.etc.Misc.doNothing;
import static wbs.utils.etc.NumberUtils.equalToOne;
import static wbs.utils.etc.NumberUtils.equalToThree;
import static wbs.utils.etc.NumberUtils.equalToTwo;
import static wbs.utils.etc.NumberUtils.moreThanZero;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringNotEqualSafe;
import static wbs.utils.web.HtmlAttributeUtils.htmlColumnSpanAttribute;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphClose;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.utils.web.HtmlFormUtils.htmlFormClose;
import static wbs.utils.web.HtmlFormUtils.htmlFormOpenMethodActionEncoding;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableHeaderCellWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenDetails;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenList;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowOpen;

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
import lombok.extern.log4j.Log4j;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang3.tuple.Pair;

import wbs.console.forms.FormField.FormType;
import wbs.console.forms.FormField.UpdateResult;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.entity.record.PermanentRecord;
import wbs.utils.string.FormatWriter;

@Log4j
@SingletonComponent ("fieldsLogic")
@SuppressWarnings ({ "rawtypes", "unchecked" })
public
class FormFieldLogic {

	public
	void implicit (
			@NonNull FormFieldSet formFieldSet,
			@NonNull Object container) {

		for (
			FormField formField
				: formFieldSet.formFields ()
		) {

			if (formField.virtual ())
				continue;

			formField.implicit (
				container);

		}

	}

	public
	UpdateResultSet update (
			@NonNull ConsoleRequestContext requestContext,
			@NonNull FormFieldSet formFieldSet,
			@NonNull Object container,
			@NonNull Map <String, Object> hints,
			@NonNull String formName) {

		UpdateResultSet updateResultSet =
			new UpdateResultSet ();

		update (
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
			@NonNull ConsoleRequestContext requestContext,
			@NonNull FormFieldSet formFieldSet,
			@NonNull UpdateResultSet updateResults,
			@NonNull Object container,
			@NonNull Map <String, Object> hints,
			@NonNull String formName) {

		update (
			requestContextToSubmission (
				requestContext),
			formFieldSet,
			updateResults,
			container,
			hints,
			formName);

	}

	public
	void update (
			@NonNull FormFieldSubmission submission,
			@NonNull FormFieldSet formFieldSet,
			@NonNull UpdateResultSet updateResults,
			@NonNull Object container,
			@NonNull Map <String,Object> hints,
			@NonNull String formName) {

		for (
			FormField formField
				: formFieldSet.formFields ()
		) {

			if (formField.virtual ())
				continue;

			UpdateResult updateResult =
				formField.update (
					submission,
					container,
					hints,
					formName);

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
					errorFieldNames.size ()));

		}

	}

	public
	void runUpdateHooks (
			@NonNull FormFieldSet formFieldSet,
			@NonNull UpdateResultSet updateResultSet,
			@NonNull Object container,
			@NonNull PermanentRecord <?> linkObject,
			@NonNull Optional <Object> objectRef,
			@NonNull Optional <String> objectType,
			@NonNull String formName) {

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
				updateResult,
				container,
				linkObject,
				objectRef,
				objectType);

		}

	}

	public
	void outputTableHeadings (
			@NonNull FormatWriter htmlWriter,
			@NonNull FormFieldSet formFieldSet) {

		for (
			FormField formField
				: formFieldSet.formFields ()
		) {

			htmlWriter.writeLineFormat (
				"<th>%h</th>",
				formField.label ());

		}

	}

	public
	void outputCsvHeadings (
			@NonNull FormatWriter csvWriter,
			@NonNull List <FormFieldSet> formFieldSets) {

		boolean first =
			true;

		for (
			FormFieldSet formFieldSet
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
					formField.label ().replace ("\"", "\"\""));

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
				requestContext.getFormData ())

			.fileItems (
				requestContext.isMultipart ()
					? Maps.uniqueIndex (
						requestContext.fileItems (),
						FileItem::getFieldName)
					: null);

	}

	public
	void outputFormRows (
			@NonNull ConsoleRequestContext requestContext,
			@NonNull FormatWriter htmlWriter,
			@NonNull FormFieldSet formFieldSet,
			@NonNull Optional <UpdateResultSet> updateResultSet,
			@NonNull Object object,
			@NonNull Map <String, Object> hints,
			@NonNull FormType formType,
			@NonNull String formName) {

		outputFormRows (
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

	public
	void outputFormAlwaysHidden (
			@NonNull ConsoleRequestContext requestContext,
			@NonNull FormatWriter htmlWriter,
			@NonNull FormFieldSet formFieldSet,
			@NonNull Optional <UpdateResultSet> updateResultSet,
			@NonNull Object object,
			@NonNull Map <String, Object> hints,
			@NonNull FormType formType,
			@NonNull String formName) {

		outputFormAlwaysHidden (
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

	public
	void outputFormAlwaysHidden (
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter htmlWriter,
			@NonNull FormFieldSet formFieldSet,
			@NonNull Optional<UpdateResultSet> updateResultSet,
			@NonNull Object object,
			@NonNull Map<String,Object> hints,
			@NonNull FormType formType,
			@NonNull String formName) {

		for (
			FormField formField
				: formFieldSet.formFields ()
		) {

			if (formField.virtual ())
				continue;

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
			@NonNull ConsoleRequestContext requestContext,
			@NonNull FormatWriter htmlWriter,
			@NonNull FormFieldSet formFieldSet,
			@NonNull Object object,
			@NonNull Map<String,Object> hints,
			@NonNull FormType formType,
			@NonNull String formName) {

		outputFormTemporarilyHidden (
			requestContextToSubmission (
				requestContext),
			htmlWriter,
			formFieldSet,
			object,
			hints,
			formType,
			formName);

	}

	public
	void outputFormTemporarilyHidden (
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter htmlWriter,
			@NonNull FormFieldSet formFieldSet,
			@NonNull Object object,
			@NonNull Map<String,Object> hints,
			@NonNull FormType formType,
			@NonNull String formName) {

		for (
			FormField formField
				: formFieldSet.formFields ()
		) {

			if (formField.virtual ())
				continue;

			if (
				! formField.canView (
					object,
					hints)
			) {
				continue;
			}

			formField.renderFormTemporarilyHidden (
				submission,
				htmlWriter,
				object,
				hints,
				formType,
				formName);

		}

	}

	public
	void outputFormRows (
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter htmlWriter,
			@NonNull FormFieldSet formFieldSet,
			@NonNull Optional <UpdateResultSet> updateResultSet,
			@NonNull Object object,
			@NonNull Map<String,Object> hints,
			@NonNull FormType formType,
			@NonNull String formName) {

		for (
			FormField formField
				: formFieldSet.formFields ()
		) {

			if (formField.virtual ())
				continue;

			if (
				! formField.canView (
					object,
					hints)
			) {
				continue;
			}

			Optional<String> error;

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

					log.error (
						stringFormat (
							"Unable to find update result for %s-%s",
							formName,
							formField.name ()));

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

			formField.renderFormRow (
				submission,
				htmlWriter,
				object,
				hints,
				error,
				formType,
				formName);

		}

	}

	public
	void outputFormReset (
			@NonNull FormatWriter javascriptWriter,
			@NonNull FormFieldSet formFieldSet,
			@NonNull FormType formType,
			@NonNull Object object,
			@NonNull Map <String, Object> hints,
			@NonNull String formName) {

		for (
			FormField formField
				: formFieldSet.formFields ()
		) {

			if (formField.virtual ())
				continue;

			formField.renderFormReset (
				javascriptWriter,
				object,
				hints,
				formType,
				formName);

		}

	}

	public
	void outputFormTable (
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
			@NonNull FormFieldSubmission submission,
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
			htmlWriter,
			method,
			actionUrl,
			enctype);

		htmlTableOpenDetails (
			htmlWriter);

		outputFormRows (
			submission,
			htmlWriter,
			formFieldSet,
			updateResultSet,
			object,
			hints,
			formType,
			formName);

		htmlTableClose (
			htmlWriter);

		htmlParagraphOpen (
			htmlWriter);

		htmlWriter.writeLineFormat (
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
			@NonNull FormatWriter htmlWriter,
			@NonNull FormFieldSet formFieldSet,
			@NonNull Object object,
			@NonNull Map<String,Object> hints) {

		htmlTableOpenDetails (
			htmlWriter);

		outputTableRows (
			htmlWriter,
			formFieldSet,
			object,
			hints);

		htmlTableClose ();

	}

	public
	void outputListTable (
			@NonNull FormatWriter htmlWriter,
			@NonNull FormFieldSet formFieldSet,
			@NonNull List <?> objects,
			boolean links) {

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
				Object object
					: objects
			) {

				htmlTableRowOpen (
					htmlWriter);

				outputTableCellsList (
					htmlWriter,
					formFieldSet,
					object,
					ImmutableMap.of (),
					links);

				htmlTableRowClose (
					htmlWriter);

			}

		}

		// table close

		htmlTableClose (
			htmlWriter);

	}

	public
	void outputCsvRow (
			@NonNull FormatWriter csvWriter,
			@NonNull List<FormFieldSet> formFieldSets,
			@NonNull Object object,
			@NonNull Map<String,Object> hints) {

		boolean first = true;

		for (
			FormFieldSet formFieldSet
				: formFieldSets
		) {

			for (
				FormField formField
					: formFieldSet.formFields ()
			) {

				if (formField.virtual ())
					continue;

				if (! first) {

					csvWriter.writeFormat (
						",");

				}

				formField.renderCsvRow (
					csvWriter,
					object,
					hints);

				first = false;

			}

		}

		csvWriter.writeFormat (
			"\n");

	}

	public
	void outputTableCellsList (
			@NonNull FormatWriter htmlWriter,
			@NonNull FormFieldSet formFieldSet,
			@NonNull Object object,
			@NonNull Map <String, Object> hints,
			@NonNull Boolean links) {

		for (
			FormField formField
				: formFieldSet.formFields ()
		) {

			if (formField.virtual ())
				continue;

			formField.renderTableCellList (
				htmlWriter,
				object,
				hints,
				links,
				1l);

		}

	}

	public
	void outputTableRowsList (
			@NonNull FormatWriter htmlWriter,
			@NonNull FormFieldSet formFieldSet,
			@NonNull Object object,
			@NonNull Boolean links,
			@NonNull Long columnSpan) {

		for (
			FormField formField
				: formFieldSet.formFields ()
		) {

			if (formField.virtual ())
				continue;

			formField.renderTableCellList (
				htmlWriter,
				object,
				ImmutableMap.of (),
				links,
				columnSpan);

		}

	}

	public
	void outputTableRows (
			@NonNull FormatWriter htmlWriter,
			@NonNull FormFieldSet formFieldSet,
			@NonNull Object object,
			@NonNull Map<String,Object> hints) {

		for (
			FormField formField
				: formFieldSet.formFields ()
		) {

			if (formField.virtual ()) {
				continue;
			}

			htmlTableRowOpen (
				htmlWriter);

			htmlTableHeaderCellWrite (
				htmlWriter,
				formField.label ());

			formField.renderTableCellProperties (
				htmlWriter,
				object,
				hints);

			htmlTableRowClose (
				htmlWriter);

		}

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
