package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.isNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;

import wbs.console.forms.FormField.UpdateResult;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.record.PermanentRecord;
import wbs.framework.utils.etc.FormatWriter;

@SingletonComponent ("fieldsLogic")
@SuppressWarnings ({ "rawtypes", "unchecked" })
public
class FormFieldLogic {

	@Inject
	ConsoleRequestContext requestContext;

	public
	UpdateResultSet update (
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
				new UpdateResult ()

				.formField (
					formField);

			formField.update (
				container,
				updateResult);

			if (updateResult.errors () != null) {

				updateResultSet.errorCount +=
					updateResult.errors ().size ();

			}

			updateResultSet.updateResults ().add (
				updateResult);

		}

		return updateResultSet;

	}

	public
	void reportErrors (
			@NonNull UpdateResultSet updateResultSet) {

		for (
			UpdateResult<?,?> updateResult
				: updateResultSet.updateResults ()
		) {

			if (
				isNull (
					updateResult.errors ())
			) {
				continue;
			}

			for (
				String error
					: updateResult.errors ()
			) {

				requestContext.addError (
					error);

			}

		}

	}

	public
	void runUpdateHooks (
			@NonNull UpdateResultSet updateResultSet,
			@NonNull Object container,
			@NonNull PermanentRecord<?> linkObject,
			@NonNull Optional<Object> objectRef,
			@NonNull Optional<String> objectType) {

		for (
			UpdateResult updateResult
				: updateResultSet.updateResults ()
		) {

			if (! updateResult.updated ())
				continue;

			FormField formField =
				updateResult.formField ();

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
	void outputFormRows (
			@NonNull FormatWriter htmlWriter,
			@NonNull FormFieldSet formFieldSet,
			@NonNull Object object) {

		for (
			FormField formField
				: formFieldSet.formFields ()
		) {

			if (formField.virtual ())
				continue;

			formField.renderFormRow (
				htmlWriter,
				object);

		}

	}

	public
	void outputFormReset (
			@NonNull FormatWriter javascriptWriter,
			@NonNull String indent,
			@NonNull FormFieldSet formFieldSet,
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
				object);

		}

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
			@NonNull Object object) {

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
				object);

			htmlWriter.writeFormat (
				"</tr>\n");

		}

	}

	@Accessors (fluent = true)
	@Data
	public static
	class UpdateResultSet {

		List<UpdateResult<?,?>> updateResults =
			new ArrayList<UpdateResult<?,?>> ();

		int errorCount;

	}

}
