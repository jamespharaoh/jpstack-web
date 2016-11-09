package wbs.console.forms;

import static wbs.utils.collection.MapUtils.mapIsNotEmpty;
import static wbs.utils.etc.EnumUtils.enumInSafe;
import static wbs.utils.etc.Misc.successResult;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringIsEmpty;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.utils.string.FormatWriter;

import fj.data.Either;

@PrototypeComponent ("textFormFieldRenderer")
@Accessors (fluent = true)
public
class TextFormFieldRenderer <Container>
	implements FormFieldRenderer <Container, String> {

	// properties

	@Getter @Setter
	String name;

	@Getter @Setter
	String label;

	@Getter @Setter
	Boolean nullable;

	@Getter @Setter
	Integer size =
		FormField.defaultSize;

	@Getter @Setter
	FormField.Align listAlign =
		FormField.Align.left;

	@Getter @Setter
	FormField.Align propertiesAlign =
		FormField.Align.left;

	@Getter @Setter
	Map <String, String> presets =
		new LinkedHashMap<> ();

	// utilities

	public
	TextFormFieldRenderer<Container> addPreset (
			@NonNull String preset) {

		presets.put (
			preset,
			preset);

		return this;

	}

	// implementation

	@Override
	public
	void renderFormTemporarilyHidden (
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <String> interfaceValue,
			@NonNull FormType formType,
			@NonNull String formName) {

		if (
			formValuePresent (
				submission,
				formName)
		) {
			interfaceValue =
				Optional.of (
					formValue (
						submission,
						formName));
		}

		htmlWriter.writeLineFormat (
			"<input",
			" type=\"hidden\"",
			" name=\"%h-%h\"",
			formName,
			name (),
			" value=\"%h\"",
			interfaceValue.or (""),
			">");

	}

	@Override
	public
	void renderFormInput (
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter out,
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<String> interfaceValue,
			@NonNull FormType formType,
			@NonNull String formName) {

		out.writeIndent ();

		out.writeFormat (
			"<input",
			" type=\"text\"",
			" id=\"%h-%h\"",
			formName,
			name (),
			" name=\"%h-%h\"",
			formName,
			name (),
			" value=\"%h\"",
			formValuePresent (
					submission,
					formName)
				? formValue (
					submission,
					formName)
				: interfaceValue.or (
					""),
			" size=\"%h\"",
			integerToDecimalString (
				size),
			">");

		if (
			mapIsNotEmpty (
				presets ())
		) {

			out.writeFormat (
				"<br>");

			out.writeNewline ();

			out.writeIndent ();

			for (
				Map.Entry<String,String> presetEntry
					: presets ().entrySet ()
			) {

				out.writeFormat (
					"<input",
					" type=\"button\"",
					" name=\"%h\"",
					stringFormat (
						"%s-%s-%s",
						formName,
						name (),
						presetEntry.getValue ()),
					" onclick=\"%h\"",
					stringFormat (
						"$('#%j-%j').val ('%j'); return false",
						formName,
						name (),
						presetEntry.getValue ()),
					" value=\"%h\"",
					presetEntry.getKey (),
					">");

			}

		}

		out.writeNewline ();

	}

	@Override
	public
	void renderFormReset (
			@NonNull FormatWriter javascriptWriter,
			@NonNull Container container,
			@NonNull Optional<String> interfaceValue,
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
				"$(\"#%j-%j\").val (\"\");",
				formName,
				name);

		} else if (
			enumInSafe (
				formType,
				FormType.update)
		) {

			javascriptWriter.writeLineFormat (
				"$(\"#%j-%j\").val (\"%j\");",
				formName,
				name,
				interfaceValue.or (""));

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
	Either <Optional <String>, String> formToInterface (
			@NonNull FormFieldSubmission submission,
			@NonNull String formName) {

		String formValue =
			formValue (
				submission,
				formName);

		if (

			nullable ()

			&& stringIsEmpty (
				formValue)

		) {

			return successResult (
				optionalAbsent ());

		}

		return successResult (
			optionalOf (
				formValue));

	}

	@Override
	public
	void renderHtmlSimple (
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <String> interfaceValue,
			boolean link) {

		htmlWriter.writeLineFormat (
			"%h",
			interfaceValue.or (""));

	}

}
