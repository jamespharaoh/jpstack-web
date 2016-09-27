package wbs.console.forms;

import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Map;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.forms.FormField.FormType;
import wbs.utils.string.FormatWriter;

import fj.data.Either;

public
interface FormFieldRenderer <Container, Interface> {

	default
	boolean fileUpload () {
		return false;
	}

	default
	FormField.Align listAlign () {
		return FormField.Align.left;
	}

	default
	FormField.Align propertiesAlign () {
		return FormField.Align.left;
	}

	void renderFormTemporarilyHidden (
			FormFieldSubmission submission,
			FormatWriter htmlWriter,
			Container container,
			Map<String,Object> hints,
			Optional<Interface> interfaceValue,
			FormType formType,
			String formName);

	void renderFormInput (
			FormFieldSubmission submission,
			FormatWriter htmlWriter,
			Container container,
			Map <String, Object> hints,
			Optional <Interface> interfaceValue,
			FormType formType,
			String formName);

	void renderFormReset (
			FormatWriter htmlWriter,
			Container container,
			Optional <Interface> interfaceValue,
			FormType formType,
			String formName);

	default
	void renderHtmlTableCell (
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <Interface> interfaceValue,
			@NonNull Boolean link,
			@NonNull Long colspan) {

		htmlWriter.writeFormat (
			"<td",
			" style=\"%h\"",
			stringFormat (
				"text-align: %s",
				propertiesAlign ().name ()));

		if (colspan != 1) {

			htmlWriter.writeFormat (
				" colspan=\"%h\"",
				colspan);

		}

		htmlWriter.writeFormat (
			">");

		renderHtmlSimple (
			htmlWriter,
			container,
			hints,
			interfaceValue,
			link);

		htmlWriter.writeFormat (
			"</td>");

	}

	void renderHtmlSimple (
			FormatWriter htmlWriter,
			Container container,
			Map<String,Object> hints,
			Optional<Interface> interfaceValue,
			boolean link);

	default
	void renderHtmlComplex (
			FormatWriter htmlWriter,
			Container container,
			Map<String,Object> hints,
			Optional<Interface> interfaceValue) {

		renderHtmlSimple (
			htmlWriter,
			container,
			hints,
			interfaceValue,
			true);

	}

	boolean formValuePresent (
			FormFieldSubmission submission,
			String formName);

	default
	Either<Optional<Interface>,String> formToInterface (
			@NonNull FormFieldSubmission submission,
			@NonNull String formName) {

		throw new UnsupportedOperationException ();

	}

	default
	Optional<String> htmlClass (
			Optional<Interface> interfaceValue) {

		return Optional.absent ();

	}

}
