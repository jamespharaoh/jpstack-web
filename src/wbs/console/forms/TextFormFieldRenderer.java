package wbs.console.forms;

import static wbs.framework.utils.etc.EnumUtils.enumInSafe;
import static wbs.framework.utils.etc.MapUtils.mapIsNotEmpty;
import static wbs.framework.utils.etc.Misc.successResult;
import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.StringUtils.stringIsEmpty;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.base.Optional;

import fj.data.Either;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.console.forms.FormField.FormType;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.FormatWriter;

@PrototypeComponent ("textFormFieldRenderer")
@Accessors (fluent = true)
public
class TextFormFieldRenderer<Container>
	implements FormFieldRenderer<Container,String> {

	// properties

	@Getter @Setter
	String name;

	@Getter @Setter
	String label;

	@Getter @Setter
	Boolean nullable;

	@Getter @Setter
	Align align;

	@Getter @Setter
	Integer size =
		FormField.defaultSize;

	@Getter @Setter
	Map<String,String> presets =
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
			@NonNull Map<String,Object> hints,
			@NonNull Optional<String> interfaceValue,
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

		htmlWriter.writeFormat (
			"<input",
			" type=\"hidden\"",
			" name=\"%h-%h\"",
			formName,
			name (),
			" value=\"%h\"",
			interfaceValue.or (""),
			">\n");

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
			size,
			">");

		if (
			mapIsNotEmpty (
				presets ())
		) {

			out.writeFormat (
				"<br>");

			for (
				Map.Entry<String,String> presetEntry
					: presets ().entrySet ()
			) {

				out.writeFormat (
					"\n<input",
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

	}

	@Override
	public
	void renderFormReset (
			@NonNull FormatWriter javascriptWriter,
			@NonNull String indent,
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

			javascriptWriter.writeFormat (
				"%s$(\"#%j-%j\").val (\"\");\n",
				indent,
				formName,
				name);

		} else if (
			enumInSafe (
				formType,
				FormType.update)
		) {

			javascriptWriter.writeFormat (
				"%s$(\"#%j-%j\").val (\"%j\");\n",
				indent,
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
				Optional.absent ());

		}

		return successResult (
			Optional.of (
				formValue));

	}

	@Override
	public
	void renderHtmlSimple (
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<String> interfaceValue,
			boolean link) {

		htmlWriter.writeFormat (
			"%h",
			interfaceValue.or (""));

	}

	// data

	public static
	enum Align {
		left,
		center,
		right;
	}

}
