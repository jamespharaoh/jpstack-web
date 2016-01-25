package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.doNothing;
import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.isEmpty;
import static wbs.framework.utils.etc.Misc.isPresent;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import org.apache.commons.fileupload.FileItem;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;

import wbs.console.forms.FormField.FormType;
import wbs.console.forms.FormField.UpdateResult;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.record.PermanentRecord;
import wbs.framework.utils.etc.FormatWriter;

@SingletonComponent ("fieldsLogic")
@SuppressWarnings ({ "rawtypes", "unchecked" })
public
class FormFieldLogic {

	public
	UpdateResultSet update (
			@NonNull ConsoleRequestContext requestContext,
			@NonNull FormFieldSet formFieldSet,
			@NonNull Object container) {

		return update (
			requestContextToSubmission (
				requestContext),
			formFieldSet,
			container);

	}

	public
	UpdateResultSet update (
			@NonNull FormFieldSubmission submission,
			@NonNull FormFieldSet formFieldSet,
			@NonNull Object container) {

		UpdateResultSet updateResultSet =
			new UpdateResultSet ();

		for (
			FormField formField
				: formFieldSet.formFields ()
		) {

			if (formField.virtual ())
				continue;

			UpdateResult updateResult =
				formField.update (
					submission,
					container);

			if (
				isPresent (
					updateResult.error ())
			) {

				updateResultSet.errorCount ++;

			} else if (
				updateResult.updated ()
			) {

				updateResultSet.updateCount ++;

			}

			updateResultSet.updateResults ().put (
				formField.name (),
				updateResult);

		}

		return updateResultSet;

	}

	public
	void reportErrors (
			@NonNull ConsoleRequestContext requestContext,
			@NonNull UpdateResultSet updateResultSet) {

		List<String> errorFieldNames =
			new ArrayList<String> ();

		for (
			Map.Entry<String,UpdateResult<?,?>> updateResultEntry
				: updateResultSet.updateResults ().entrySet ()
		) {

			String formFieldName =
				updateResultEntry.getKey ();

			UpdateResult updateResult =
				updateResultEntry.getValue ();

			if (
				isPresent (
					updateResult.error ())
			) {

				errorFieldNames.add (
					formFieldName);

			}

		}

		if (
			isEmpty (
				errorFieldNames)
		) {

			doNothing ();

		} else if (
			equal (
				errorFieldNames.size (),
				1)
		) {

			requestContext.addError (
				stringFormat (
					"The '%s' field is not valid",
					errorFieldNames.get (0)));

		} else if (
			equal (
				errorFieldNames.size (),
				2)
		) {

			requestContext.addError (
				stringFormat (
					"The '%s' and '%s' fields are invalid",
					errorFieldNames.get (0),
					errorFieldNames.get (1)));

		} else if (
			equal (
				errorFieldNames.size (),
				3)
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
			@NonNull PermanentRecord<?> linkObject,
			@NonNull Optional<Object> objectRef,
			@NonNull Optional<String> objectType) {

		for (
			Map.Entry updateResultEntry
				: updateResultSet.updateResults ().entrySet ()
		) {

			FormField formField =
				formFieldSet.formField (
					(String)
					updateResultEntry.getKey ());

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
			@NonNull FormType formType) {

		outputFormRows (
			requestContextToSubmission (
				requestContext),
			htmlWriter,
			formFieldSet,
			updateResultSet,
			object,
			hints,
			formType);

	}

	public
	void outputFormRows (
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter htmlWriter,
			@NonNull FormFieldSet formFieldSet,
			@NonNull Optional<UpdateResultSet> updateResultSet,
			@NonNull Object object,
			@NonNull Map<String,Object> hints,
			@NonNull FormType formType) {

		for (
			FormField formField
				: formFieldSet.formFields ()
		) {

			if (formField.virtual ())
				continue;

			Optional<String> error;

			if (
				isPresent (
					updateResultSet)
			) {

				UpdateResult updateResult =
					updateResultSet.get ().updateResults ().get (
						formField.name ());

				error =
					updateResult.error ();

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
				formType);

		}

	}

	public
	void outputFormReset (
			@NonNull FormatWriter javascriptWriter,
			@NonNull String indent,
			@NonNull FormFieldSet formFieldSet,
			@NonNull FormType formType,
			@NonNull Object object) {

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
				formType);

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
			@NonNull String actionUrl,
			@NonNull String submitButtonLabel,
			@NonNull FormType formType) {

		outputFormTable (
			requestContextToSubmission (
				requestContext),
			htmlWriter,
			formFieldSet,
			updateResultSet,
			object,
			hints,
			actionUrl,
			submitButtonLabel,
			formType);

	}

	public
	void outputFormTable (
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter htmlWriter,
			@NonNull FormFieldSet formFieldSet,
			@NonNull Optional<UpdateResultSet> updateResultSet,
			@NonNull Object object,
			@NonNull Map<String,Object> hints,
			@NonNull String actionUrl,
			@NonNull String submitButtonLabel,
			@NonNull FormType formType) {

		htmlWriter.writeFormat (
			"<form",
			" method=\"post\"",
			" action=\"%h\"",
			actionUrl,
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
			formType);

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
			isEmpty (
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
			@NonNull Object object) {

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
					object);

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

		Map<String,UpdateResult<?,?>> updateResults =
			new LinkedHashMap<String,UpdateResult<?,?>> ();

		int errorCount;
		int updateCount;

	}

}
