package wbs.console.forms;

import static wbs.framework.utils.etc.CollectionUtils.collectionIsEmpty;
import static wbs.framework.utils.etc.Misc.doNothing;
import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.NumberUtils.equalToOne;
import static wbs.framework.utils.etc.NumberUtils.equalToThree;
import static wbs.framework.utils.etc.NumberUtils.equalToTwo;
import static wbs.framework.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.StringUtils.stringNotEqualSafe;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;
import wbs.console.forms.FormField.FormType;
import wbs.console.forms.FormField.UpdateResult;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.entity.record.PermanentRecord;
import wbs.framework.utils.formatwriter.FormatWriter;

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
			@NonNull Map<String,Object> hints,
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
			@NonNull Map<String,Object> hints,
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
			@NonNull Map<String,Object> hints,
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

		List<String> errorFieldNames =
			new ArrayList<String> ();

		for (
			Map.Entry<Pair<String,String>,UpdateResult<?,?>> updateResultEntry
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

			htmlWriter.writeFormat (
				"<th>%h</th>\n",
				formField.label ());

		}

	}

	public
	void outputCsvHeadings (
			@NonNull FormatWriter csvWriter,
			@NonNull List<FormFieldSet> formFieldSets) {

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
			@NonNull Optional<UpdateResultSet> updateResultSet,
			@NonNull Object object,
			@NonNull Map<String,Object> hints,
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
			@NonNull Optional<UpdateResultSet> updateResultSet,
			@NonNull Object object,
			@NonNull Map<String,Object> hints,
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

			Optional<String> error;

			if (
				optionalIsPresent (
					updateResultSet)
			) {

				UpdateResult updateResult =
					updateResultSet.get ().updateResults ().get (
						stringFormat (
							"%s-%s",
							formName,
							formField.name ()));

				if (
					isNull (
						updateResult)
				) {

					log.error (
						stringFormat (
							"Unable to find update result for %s-%s",
							formName,
							formField.name ()));

					error =
						Optional.absent ();

				} else {

					error =
						updateResult.error ();

				}

			} else {

				error =
					Optional.<String>absent ();

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
			@NonNull String indent,
			@NonNull FormFieldSet formFieldSet,
			@NonNull FormType formType,
			@NonNull Object object,
			@NonNull Map<String,Object> hints,
			@NonNull String formName) {

		for (
			FormField formField
				: formFieldSet.formFields ()
		) {

			if (formField.virtual ())
				continue;

			formField.renderFormReset (
				javascriptWriter,
				indent,
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
			@NonNull Optional<UpdateResultSet> updateResultSet,
			@NonNull Object object,
			@NonNull Map<String,Object> hints,
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
			@NonNull Optional<UpdateResultSet> updateResultSet,
			@NonNull Object object,
			@NonNull Map<String,Object> hints,
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

		htmlWriter.writeFormat (
			"<form",
			" method=\"%h\"",
			method,
			" action=\"%h\"",
			actionUrl,
			" enctype=\"%h\"",
			enctype,
			">\n");

		htmlWriter.writeFormat (
			"<table class=\"details\">\n");

		outputFormRows (
			submission,
			htmlWriter,
			formFieldSet,
			updateResultSet,
			object,
			hints,
			formType,
			formName);

		htmlWriter.writeFormat (
			"</table>\n");

		htmlWriter.writeFormat (
			"<p><input",
			" type=\"submit\"",
			" value=\"%h\"",
			submitButtonLabel,
			"></p>\n");

		htmlWriter.writeFormat (
			"</form>\n");

	}

	public
	void outputDetailsTable (
			@NonNull FormatWriter htmlWriter,
			@NonNull FormFieldSet formFieldSet,
			@NonNull Object object,
			@NonNull Map<String,Object> hints) {

		htmlWriter.writeFormat (
			"<table class=\"details\">\n");

		outputTableRows (
			htmlWriter,
			formFieldSet,
			object,
			hints);

		htmlWriter.writeFormat (
			"</table>\n");

	}

	public
	void outputListTable (
			@NonNull FormatWriter htmlWriter,
			@NonNull FormFieldSet formFieldSet,
			@NonNull List<?> objects,
			boolean links) {

		htmlWriter.writeFormat (
			"<table class=\"list\">\n");

		htmlWriter.writeFormat (
			"<tr>\n");

		outputTableHeadings (
			htmlWriter,
			formFieldSet);

		htmlWriter.writeFormat (
			"</tr>");

		if (
			collectionIsEmpty (
				objects)
		) {

			htmlWriter.writeFormat (
				"<tr><td",
				" colspan=\"%h\"",
				formFieldSet.columns (),
				">%h</td></tr>\n",
				"There is no data to display");

		} else {

			for (
				Object object
					: objects
			) {

				htmlWriter.writeFormat (
					"<tr>\n");

				outputTableCellsList (
					htmlWriter,
					formFieldSet,
					object,
					ImmutableMap.of (),
					links);

				htmlWriter.writeFormat (
					"</tr>\n");

			}

		}

		htmlWriter.writeFormat (
			"</table>\n");

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
			@NonNull Map<String,Object> hints,
			boolean links) {

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
				1);

		}

	}

	public
	void outputTableRowsList (
			@NonNull FormatWriter htmlWriter,
			@NonNull FormFieldSet formFieldSet,
			@NonNull Object object,
			boolean links,
			int colspan) {

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
				colspan);

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

			htmlWriter.writeFormat (
				"<tr>\n",
				"<th>%h</th>\n",
				formField.label ());

			formField.renderTableCellProperties (
				htmlWriter,
				object,
				hints);

			htmlWriter.writeFormat (
				"</tr>\n");

		}

	}

	@Accessors (fluent = true)
	@Data
	public static
	class UpdateResultSet {

		Map<Pair<String,String>,UpdateResult<?,?>> updateResults =
			new LinkedHashMap<> ();

		int errorCount;
		int updateCount;

	}

}
