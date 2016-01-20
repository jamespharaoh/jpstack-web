package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.booleanToString;
import static wbs.framework.utils.etc.Misc.in;
import static wbs.framework.utils.etc.Misc.isNotPresent;
import static wbs.framework.utils.etc.Misc.isPresent;
import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.stringToBoolean;
import static wbs.framework.utils.etc.Misc.successResult;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;

import fj.data.Either;

import wbs.console.forms.FormField.FormType;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.FormatWriter;

@Accessors (fluent = true)
@PrototypeComponent ("yesNoFormFieldRenderer")
public
class YesNoFormFieldRenderer<Container>
	implements FormFieldRenderer<Container,Boolean> {

	// properties

	@Getter @Setter
	String name;

	@Getter @Setter
	String label;

	@Getter @Setter
	Boolean nullable;

	@Getter @Setter
	String yesLabel;

	@Getter @Setter
	String noLabel;

	// details

	@Getter
	boolean fileUpload = false;

	// implementation

	@Override
	public
	void renderTableCellList (
			@NonNull FormatWriter out,
			@NonNull Container container,
			@NonNull Optional<Boolean> interfaceValue,
			boolean link,
			int colspan) {

		out.writeFormat (
			"<td",
			colspan > 1
				? stringFormat (
					" colspan=\"%h\"",
					colspan)
				: "",
			">%s</td>\n",
			interfaceToHtmlSimple (
				container,
				interfaceValue,
				link));

	}

	@Override
	public
	void renderTableCellProperties (
			@NonNull FormatWriter out,
			@NonNull Container container,
			@NonNull Optional<Boolean> interfaceValue) {

		out.writeFormat (
			"<td>%s</td>\n",
			interfaceToHtmlComplex (
				container,
				interfaceValue));

	}

	@Override
	public
	void renderTableRow (
			@NonNull FormatWriter out,
			@NonNull Container container,
			@NonNull Optional<Boolean> interfaceValue) {

		out.writeFormat (
			"<tr>\n",
			"<th>%h</th>\n",
			label ());

		renderTableCellProperties (
			out,
			container,
			interfaceValue);

		out.writeFormat (
			"</tr>\n");

	}

	@Override
	public
	void renderFormRow (
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter out,
			@NonNull Container container,
			@NonNull Optional<Boolean> interfaceValue,
			@NonNull Optional<String> error,
			@NonNull FormType formType) {

		out.writeFormat (
			"<tr>\n",
			"<th>%h</th>\n",
			label (),
			"<td>");

		renderFormInput (
			submission,
			out,
			container,
			interfaceValue,
			formType);

		if (
			isPresent (
				error)
		) {

			out.writeFormat (
				"<br>\n",
				"%h",
				error.get ());

		}

		out.writeFormat (
			"</td>\n",
			"</tr>\n");

	}

	@Override
	public
	void renderFormInput (
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Optional<Boolean> interfaceValue,
			@NonNull FormType formType) {

		Optional<Boolean> currentValue =
			formValuePresent (
					submission)
				? Optional.fromNullable (
					stringToBoolean (
						formValue (
							submission),
						"yes",
						"no",
						"none"))
				: interfaceValue;

		htmlWriter.writeFormat (
			"<select name=\"%h\">",
			name ());

		if (

			nullable ()

			|| isNotPresent (
				currentValue)

			|| in (
				formType,
				FormType.create,
				FormType.perform,
				FormType.search)

		) {

			htmlWriter.writeFormat (
				"<option",
				" value=\"none\"",
				currentValue.isPresent ()
					? ""
					: " selected",
				">&mdash;</option>\n");

		}

		htmlWriter.writeFormat (
			"<option",
			" value=\"yes\"",
			currentValue.or (false) == true
				? " selected"
				: "",
			">%h</option>\n",
			yesLabel ());

		htmlWriter.writeFormat (
			"<option",
			" value=\"no\"",
			currentValue.or (true) == false
				? " selected"
				: "",
			">%h</option>\n",
			noLabel ());

		htmlWriter.writeFormat (
			"</select>");

	}

	@Override
	public
	void renderFormReset (
			@NonNull FormatWriter javascriptWriter,
			@NonNull String indent,
			@NonNull Container container,
			@NonNull Optional<Boolean> interfaceValue,
			@NonNull FormType formType) {

		if (
			in (
				formType,
				FormType.create,
				FormType.perform,
				FormType.search)
		) {

			javascriptWriter.writeFormat (
				"%s$(\"#%j\").val (\"none\");\n",
				indent,
				name);

		} else if (
			in (
				formType,
				FormType.update)
		) {

			javascriptWriter.writeFormat (
				"%s$(\"#%j\").val (%s);\n",
				indent,
				name,
				booleanToString (
					interfaceValue.orNull (),
					"true",
					"false",
					"none"));

		} else {

			throw new RuntimeException ();

		}

	}

	@Override
	public
	boolean formValuePresent (
			@NonNull FormFieldSubmission submission) {

		return submission.hasParameter (
			name ());

	}

	String formValue (
			@NonNull FormFieldSubmission submission) {

		return submission.parameter (
			name ());

	}

	@Override
	public
	Either<Optional<Boolean>,String> formToInterface (
			@NonNull FormFieldSubmission submission) {

		String formValue =
			formValue (
				submission);

		return successResult (
			Optional.fromNullable (
				stringToBoolean (
					formValue)));

	}

	@Override
	public
	String interfaceToHtmlSimple (
			@NonNull Container container,
			@NonNull Optional<Boolean> genericValue,
			boolean link) {

		return stringFormat (
			"%h",
			booleanToString (
				genericValue.orNull (),
				yesLabel (),
				noLabel (),
				"-"));

	}

	@Override
	public
	String interfaceToHtmlComplex (
			@NonNull Container container,
			@NonNull Optional<Boolean> genericValue) {

		return interfaceToHtmlSimple (
			container,
			genericValue,
			true);

	}

}
