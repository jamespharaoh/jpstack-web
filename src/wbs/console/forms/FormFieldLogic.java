package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.console.forms.FormField.UpdateResult;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.record.PermanentRecord;

@SingletonComponent ("fieldsLogic")
@SuppressWarnings ({ "rawtypes", "unchecked" })
public
class FormFieldLogic {

	@Inject
	ConsoleRequestContext requestContext;

	public
	UpdateResultSet update (
			FormFieldSet formFieldSet,
			Object container) {

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
			UpdateResultSet updateResultSet) {

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
			UpdateResultSet updateResultSet,
			Object container,
			PermanentRecord<?> linkObject,
			Object objectRef,
			String objectType) {

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
			PrintWriter out,
			FormFieldSet formFieldSet) {

		for (
			FormField formField
				: formFieldSet.formFields ()
		) {

			out.print (
				stringFormat (
					"<th>%h</th>\n",
					formField.label ()));

		}

	}

	public
	void outputCsvHeadings (
			PrintWriter out,
			FormFieldSet formFieldSet) {

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
			PrintWriter out,
			FormFieldSet formFieldSet,
			Object object) {

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
	void outputCsvRow (
			PrintWriter out,
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
				out.write (",");
			}

			formField.renderCsvRow (
				out,
				object);

			first = false;

		}

		out.write ("\n");

	}

	public
	void outputTableCellsList (
			PrintWriter out,
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
				links);

		}

	}

	public
	void outputTableRows (
			PrintWriter out,
			FormFieldSet formFieldSet,
			Object object) {

		for (FormField formField
				: formFieldSet.formFields ()) {

			if (formField.virtual ())
				continue;

			out.print (
				stringFormat (
					"<tr>\n",
					"<th>%h</th>\n",
					formField.label ()));

			formField.renderTableCellProperties (
				out,
				object);

			out.print (
				stringFormat (
					"</tr>\n"));

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
