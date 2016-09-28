package wbs.console.forms;

import static wbs.utils.etc.EnumUtils.enumInSafe;
import static wbs.utils.etc.LogicUtils.booleanToString;
import static wbs.utils.etc.LogicUtils.booleanToTrueFalseNone;
import static wbs.utils.etc.LogicUtils.booleanToYesNoNone;
import static wbs.utils.etc.Misc.doNothing;
import static wbs.utils.etc.Misc.stringToBoolean;
import static wbs.utils.etc.Misc.successResult;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Map;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.forms.FormField.FormType;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.utils.string.FormatWriter;

import fj.data.Either;

@Accessors (fluent = true)
@PrototypeComponent ("yesNoFormFieldRenderer")
public
class YesNoFormFieldRenderer <Container>
	implements FormFieldRenderer <Container, Boolean> {

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
			@NonNull Map <String, Object> hints,
			@NonNull Optional <Boolean> interfaceValue,
			@NonNull FormType formType,
			@NonNull String formName) {

		htmlWriter.writeFormat (
			"<input",
			" type=\"hidden\"",
			" name=\"%h-%h\"",
			formName,
			name (),
			" value=\"%h\"",
			booleanToYesNoNone (
				interfaceValue),
			">\n");

		doNothing ();

	}

	@Override
	public
	void renderFormInput (
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <Boolean> interfaceValue,
			@NonNull FormType formType,
			@NonNull String formName) {

		Optional <Boolean> currentValue =
			formValuePresent (
					submission,
					formName)
				? Optional.fromNullable (
					stringToBoolean (
						formValue (
							submission,
							formName),
						"yes",
						"no",
						"none"))
				: interfaceValue;

		htmlWriter.writeFormat (
			"<select",
			" name=\"%h-%h\"",
			formName,
			name (),
			">");

		if (

			nullable ()

			|| optionalIsNotPresent (
				currentValue)

			|| enumInSafe (
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
			@NonNull Container container,
			@NonNull Optional <Boolean> interfaceValue,
			@NonNull FormType formType,
			@NonNull String formName) {

		if (
			enumInSafe (
				formType,
				FormType.create,
				FormType.perform,
				FormType.search)
		) {

			javascriptWriter.writeLineFormat (
				"$(\"#%j-%j\").val (\"none\");",
				formName,
				name);

		} else if (
			enumInSafe (
				formType,
				FormType.update)
		) {

			javascriptWriter.writeLineFormat (
				"$(\"#%j-%j\").val (%s);",
				formName,
				name,
				booleanToTrueFalseNone (
					interfaceValue));

		} else {

			throw new RuntimeException ();

		}

	}

	@Override
	public
	boolean formValuePresent (
			@NonNull FormFieldSubmission submission,
			@NonNull String formName) {

		return submission.hasParameter (
			stringFormat (
				"%s-%s",
				formName,
				name ()));

	}

	String formValue (
			@NonNull FormFieldSubmission submission,
			@NonNull String formName) {

		return submission.parameter (
			stringFormat (
				"%s-%s",
				formName,
				name ()));

	}

	@Override
	public
	Either <Optional <Boolean>, String> formToInterface (
			@NonNull FormFieldSubmission submission,
			@NonNull String formName) {

		String formValue =
			formValue (
				submission,
				formName);

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
	void renderHtmlSimple (
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <Boolean> genericValue,
			boolean link) {

		htmlWriter.writeFormat (
			"%h",
			booleanToString (
				genericValue,
				yesLabel (),
				noLabel (),
				"—"));

	}

}
