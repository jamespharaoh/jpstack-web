package wbs.console.forms;

import static wbs.framework.utils.etc.EnumUtils.enumInSafe;
import static wbs.framework.utils.etc.Misc.requiredSuccess;
import static wbs.framework.utils.etc.Misc.successResult;
import static wbs.framework.utils.etc.Misc.toEnum;
import static wbs.framework.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.framework.utils.etc.OptionalUtils.optionalMapOptional;
import static wbs.framework.utils.etc.StringUtils.camelToHyphen;
import static wbs.framework.utils.etc.StringUtils.hyphenToCamel;
import static wbs.framework.utils.etc.StringUtils.stringEqualSafe;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.Map;

import com.google.common.base.Optional;

import fj.data.Either;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.console.forms.FormField.FormType;
import wbs.console.helper.EnumConsoleHelper;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.formatwriter.FormatWriter;

@Accessors (fluent = true)
@PrototypeComponent ("enumFormFieldRenderer")
public
class EnumFormFieldRenderer<Container,Interface extends Enum<Interface>>
	implements FormFieldRenderer<Container,Interface> {

	// properties

	@Getter @Setter
	String name;

	@Getter @Setter
	String label;

	@Getter @Setter
	Boolean nullable;

	@Getter @Setter
	EnumConsoleHelper<Interface> enumConsoleHelper;

	// implementation

	@Override
	public
	void renderFormTemporarilyHidden (
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<Interface> interfaceValue,
			@NonNull FormType formType,
			@NonNull String formName) {

		htmlWriter.writeFormat (
			"<input",
			" type=\"hidden\"",
			" name=\"%h-%h\"",
			formName,
			name (),
			" value=\"%h\"",
			interfaceValue.isPresent ()
				? camelToHyphen (
					interfaceValue.get ().name ())
				: "none",
			">\n");

	}

	@Override
	public
	void renderFormInput (
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<Interface> interfaceValue,
			@NonNull FormType formType,
			@NonNull String formName) {

		Optional<Interface> currentValue =
			formValuePresent (
					submission,
					formName)
				? requiredSuccess (
					formToInterface (
						submission,
						formName))
				: interfaceValue;

		htmlWriter.writeFormat (
			"<select",
			" id=\"%h-%h\"",
			formName,
			name,
			" name=\"%h-%h\"",
			formName,
			name,
			">\n");

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

			htmlWriter.writeFormat (
				"<option",
				" value=\"none\"",
				currentValue.isPresent ()
					? ""
					: " selected",
				">&mdash;</option>\n");

		}

		for (
			Map.Entry<Interface,String> optionEntry
				: enumConsoleHelper.map ().entrySet ()
		) {

			Interface optionValue =
				optionEntry.getKey ();

			String optionLabel =
				optionEntry.getValue ();

			htmlWriter.writeFormat (
				"<option",
				" value=\"%h\"",
				camelToHyphen (
					optionValue.name ()),
				optionValue == currentValue.orNull ()
					? " selected"
					: "",
				">%h</option>\n",
				optionLabel);

		}

		htmlWriter.writeFormat (
			"</select>\n");

	}

	@Override
	public
	void renderFormReset (
			@NonNull FormatWriter javascriptWriter,
			@NonNull String indent,
			@NonNull Container container,
			@NonNull Optional<Interface> interfaceValue,
			@NonNull FormType formType,
			@NonNull String formName) {

		if (
			enumInSafe (
				formType,
				FormType.create,
				FormType.perform,
				FormType.search)
		) {

			javascriptWriter.writeFormat (
				"%s$(\"#%j-%j\").val (\"none\");\n",
				indent,
				formName,
				name);

		} else if (
			enumInSafe (
				formType,
				FormType.update)
		) {

			javascriptWriter.writeFormat (
				"%s$(\"#%j-%j\").val (\"%h\");\n",
				indent,
				formName,
				name,
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
				"%s-%s",
				formName,
				name ()));

	}

	@Override
	public
	Either<Optional<Interface>,String> formToInterface (
			@NonNull FormFieldSubmission submission,
			@NonNull String formName) {

		String parameterValue =
			submission.parameter (
				stringFormat (
					"%s-%s",
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
									"%s-%s",
									formName,
									name ()))))));

		}

	}

	@Override
	public
	void renderHtmlSimple (
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<Interface> interfaceValue,
			boolean link) {

		htmlWriter.writeFormat (
			"%h",
			interfaceValue.isPresent ()
				? interfaceValue.get ().toString ()
				: "");

	}

	@Override
	public 
	Optional<String> htmlClass (
			@NonNull Optional<Interface> interfaceValueOptional) {

		return optionalMapOptional (
			interfaceValueOptional,
			enumConsoleHelper::htmlClass);

	}

}
