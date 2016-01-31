package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.booleanToString;
import static wbs.framework.utils.etc.Misc.doNothing;
import static wbs.framework.utils.etc.Misc.in;
import static wbs.framework.utils.etc.Misc.isNotPresent;
import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.stringToBoolean;
import static wbs.framework.utils.etc.Misc.successResult;

import java.util.Map;

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

	// implementation

	@Override
	public
	void renderFormTemporarilyHidden (
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<Boolean> interfaceValue,
			@NonNull FormType formType) {

		htmlWriter.writeFormat (
			"<input",
			" type=\"hidden\"",
			" name=\"%h\"",
			name (),
			" value=\"%h\"",
			booleanToString (
				interfaceValue.orNull (),
				"yes",
				"no",
				"none"),
			">\n");

		doNothing ();

	}

	@Override
	public
	void renderFormInput (
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
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
					formValue,
					"yes",
					"no",
					"none")));

	}

	@Override
	public
	String interfaceToHtmlSimple (
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
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
			@NonNull Map<String,Object> hints,
			@NonNull Optional<Boolean> genericValue) {

		return interfaceToHtmlSimple (
			container,
			hints,
			genericValue,
			true);

	}

}
