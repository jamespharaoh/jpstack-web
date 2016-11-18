package wbs.console.forms;

import static wbs.utils.etc.EnumUtils.enumInSafe;
import static wbs.utils.etc.LogicUtils.booleanToString;
import static wbs.utils.etc.LogicUtils.booleanToTrueFalseNone;
import static wbs.utils.etc.LogicUtils.booleanToYesNoNone;
import static wbs.utils.etc.LogicUtils.parseBooleanYesNoNone;
import static wbs.utils.etc.Misc.doNothing;
import static wbs.utils.etc.Misc.successResult;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Map;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

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

		htmlWriter.writeLineFormat (
			"<input",
			" type=\"hidden\"",
			" name=\"%h.%h\"",
			formName,
			name (),
			" value=\"%h\"",
			booleanToYesNoNone (
				interfaceValue),
			">");

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
				? parseBooleanYesNoNone (
					formValue (
						submission,
						formName))
				: interfaceValue;

		htmlWriter.writeLineFormat (
			"<select",
			" name=\"%h.%h\"",
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

			htmlWriter.writeLineFormat (
				"<option",
				" value=\"none\"",
				currentValue.isPresent ()
					? ""
					: " selected",
				">&mdash;</option>");

		}

		htmlWriter.writeLineFormat (
			"<option",
			" value=\"yes\"",
			currentValue.or (false) == true
				? " selected"
				: "",
			">%h</option>",
			yesLabel ());

		htmlWriter.writeLineFormat (
			"<option",
			" value=\"no\"",
			currentValue.or (true) == false
				? " selected"
				: "",
			">%h</option>",
			noLabel ());

		htmlWriter.writeLineFormat (
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
				"$(\"#%j.%j\").val (\"none\");",
				formName,
				name);

		} else if (
			enumInSafe (
				formType,
				FormType.update)
		) {

			javascriptWriter.writeLineFormat (
				"$(\"#%j.%j\").val (%s);",
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
				"%s.%s",
				formName,
				name ()));

	}

	String formValue (
			@NonNull FormFieldSubmission submission,
			@NonNull String formName) {

		return submission.parameter (
			stringFormat (
				"%s.%s",
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
			parseBooleanYesNoNone (
				formValue));

	}

	@Override
	public
	void renderHtmlSimple (
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <Boolean> genericValue,
			boolean link) {

		htmlWriter.writeLineFormat (
			"%h",
			booleanToString (
				genericValue,
				yesLabel (),
				noLabel (),
				"—"));

	}

}
