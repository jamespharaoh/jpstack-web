package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.isNull;

import java.io.IOException;
import java.io.PrintWriter;
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
			@NonNull FormatWriter out,
			@NonNull FormFieldSet formFieldSet) {

		for (
			FormField formField
				: formFieldSet.formFields ()
		) {

			out.writeFormat (
				"<th>%h</th>\n",
				formField.label ());

		}

	}

	public
	void outputCsvHeadings (
			@NonNull PrintWriter out,
			@NonNull FormFieldSet formFieldSet) {

		boolean first = true;

		for (
			FormField formField
				: formFieldSet.formFields ()
		) {

			if (! first)
				out.write (",");

			out.write ("\"");

			out.write (
				formField.label ().replace ("\"", "\"\""));

			out.write ("\"");

			first = false;

		}

		out.write ("\n");

	}

	public
	void outputFormRows (
			@NonNull FormatWriter out,
			@NonNull FormFieldSet formFieldSet,
			@NonNull Object object) {

		for (
			FormField formField
				: formFieldSet.formFields ()
		) {

			if (formField.virtual ())
				continue;

			formField.renderFormRow (
				out,
				object);

		}

	}

	public
	void outputFormReset (
			FormatWriter javascriptWriter,
			String indent,
			FormFieldSet formFieldSet,
			Object object) {

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
			FormatWriter out,
			FormFieldSet formFieldSet,
			Object object)
		throws IOException {

		boolean first = true;

		for (
			FormField formField
				: formFieldSet.formFields ()
		) {

			if (formField.virtual ())
				continue;

			if (! first) {

				out.writeFormat (
					",");

			}

			formField.renderCsvRow (
				out,
				object);

			first = false;

		}

		out.writeFormat (
			"\n");

	}

	public
	void outputTableCellsList (
			FormatWriter out,
			FormFieldSet formFieldSet,
			Object object,
			boolean links) {

		for (
			FormField formField
				: formFieldSet.formFields ()
		) {

			if (formField.virtual ())
				continue;

			formField.renderTableCellList (
				out,
				object,
				links,
				1);

		}

	}

	public
	void outputTableRowsList (
			FormatWriter out,
			FormFieldSet formFieldSet,
			Object object,
			boolean links,
			int colspan) {

		for (
			FormField formField
				: formFieldSet.formFields ()
		) {

			if (formField.virtual ())
				continue;

			formField.renderTableCellList (
				out,
				object,
				links,
				colspan);

		}

	}

	public
	void outputTableRows (
			@NonNull FormatWriter out,
			@NonNull FormFieldSet formFieldSet,
			@NonNull Object object) {

		for (
			FormField formField
				: formFieldSet.formFields ()
		) {

			if (formField.virtual ()) {
				continue;
			}

			out.writeFormat (
				"<tr>\n",
				"<th>%h</th>\n",
				formField.label ());

			formField.renderTableCellProperties (
				out,
				object);

			out.writeFormat (
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
