package wbs.console.forms;

import static wbs.utils.etc.EnumUtils.enumInSafe;
import static wbs.utils.etc.Misc.requiredSuccess;
import static wbs.utils.etc.Misc.successResult;
import static wbs.utils.etc.Misc.toEnum;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalMapOptional;
import static wbs.utils.string.StringUtils.camelToHyphen;
import static wbs.utils.string.StringUtils.hyphenToCamel;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Map;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.helper.enums.EnumConsoleHelper;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.logging.TaskLogger;

import wbs.utils.string.FormatWriter;

import fj.data.Either;

@Accessors (fluent = true)
@PrototypeComponent ("enumFormFieldRenderer")
public
class EnumFormFieldRenderer <Container, Interface extends Enum <Interface>>
	implements FormFieldRenderer <Container, Interface> {

	// properties

	@Getter @Setter
	String name;

	@Getter @Setter
	String label;

	@Getter @Setter
	Boolean nullable;

	@Getter @Setter
	EnumConsoleHelper <Interface> enumConsoleHelper;

	// implementation

	@Override
	public
	void renderFormTemporarilyHidden (
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <Interface> interfaceValue,
			@NonNull FormType formType,
			@NonNull String formName) {

		htmlWriter.writeLineFormat (
			"<input",
			" type=\"hidden\"",
			" name=\"%h.%h\"",
			formName,
			name (),
			" value=\"%h\"",
			interfaceValue.isPresent ()
				? camelToHyphen (
					interfaceValue.get ().name ())
				: "none",
			">");

	}

	@Override
	public
	void renderFormInput (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <Interface> interfaceValue,
			@NonNull FormType formType,
			@NonNull String formName) {

		Optional <Interface> currentValue =
			formValuePresent (
					submission,
					formName)
				? requiredSuccess (
					formToInterface (
						submission,
						formName))
				: interfaceValue;

		htmlWriter.writeLineFormatIncreaseIndent (
			"<select",
			" id=\"%h.%h\"",
			formName,
			name,
			" name=\"%h.%h\"",
			formName,
			name,
			">");

		if (

			nullable ()

			|| optionalIsNotPresent (
				currentValue)

			|| enumInSafe (
				formType,
				FormType.create,
				FormType.search,
				FormType.update)

		) {

			htmlWriter.writeLineFormat (
				"<option",
				" value=\"none\"",
				currentValue.isPresent ()
					? ""
					: " selected",
				">&mdash;</option>");

		}

		for (
			Map.Entry <Interface, String> optionEntry
				: enumConsoleHelper.map ().entrySet ()
		) {

			Interface optionValue =
				optionEntry.getKey ();

			String optionLabel =
				optionEntry.getValue ();

			htmlWriter.writeLineFormat (
				"<option",
				" value=\"%h\"",
				camelToHyphen (
					optionValue.name ()),
				optionValue == currentValue.orNull ()
					? " selected"
					: "",
				">%h</option>",
				optionLabel);

		}

		htmlWriter.writeLineFormatDecreaseIndent (
			"</select>");

	}

	@Override
	public
	void renderFormReset (
			@NonNull FormatWriter javascriptWriter,
			@NonNull Container container,
			@NonNull Optional <Interface> interfaceValue,
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
				"$(\"%j\").val (\"none\");",
				stringFormat (
					"#%s\\.%s",
					formName,
					name));

		} else if (
			enumInSafe (
				formType,
				FormType.update)
		) {

			javascriptWriter.writeLineFormat (
				"$(\"%j\").val (\"%h\");",
				stringFormat (
					"#%s\\.%s",
					formName,
					name),
				interfaceValue.isPresent ()
					? camelToHyphen (
						interfaceValue.get ().name ())
					: "none");

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

	@Override
	public
	Either <Optional <Interface>, String> formToInterface (
			@NonNull FormFieldSubmission submission,
			@NonNull String formName) {

		String parameterValue =
			submission.parameter (
				stringFormat (
					"%s.%s",
					formName,
					name ()));

		if (
			stringEqualSafe (
				parameterValue,
				"none")
		) {

			return successResult (
				Optional.absent ());

		} else {

			return successResult (
				Optional.of (
					toEnum (
						enumConsoleHelper.enumClass (),
						hyphenToCamel (
							submission.parameter (
								stringFormat (
									"%s.%s",
									formName,
									name ()))))));

		}

	}

	@Override
	public
	void renderHtmlSimple (
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <Interface> interfaceValue,
			boolean link) {

		htmlWriter.writeFormat (
			"%h",
			interfaceValue.isPresent ()
				? interfaceValue.get ().toString ()
				: "");

	}

	@Override
	public
	Optional <String> htmlClass (
			@NonNull Optional <Interface> interfaceValueOptional) {

		return optionalMapOptional (
			interfaceValueOptional,
			enumConsoleHelper::htmlClass);

	}

}
