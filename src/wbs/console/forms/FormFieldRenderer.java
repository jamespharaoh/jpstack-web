package wbs.console.forms;

import java.util.Map;

import lombok.NonNull;

import com.google.common.base.Optional;

import fj.data.Either;

import wbs.console.forms.FormField.FormType;
import wbs.framework.utils.etc.FormatWriter;

public
interface FormFieldRenderer<Container,Interface> {

	default
	boolean fileUpload () {
		return false;
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
			Map<String,Object> hints,
			Optional<Interface> interfaceValue,
			FormType formType,
			String formName);

	void renderFormReset (
			FormatWriter htmlWriter,
			String indent,
			Container container,
			Optional<Interface> interfaceValue,
			FormType formType,
			String formName);

	default
	void renderHtmlTableCell (
			FormatWriter htmlWriter,
			Container container,
			Map<String,Object> hints,
			Optional<Interface> interfaceValue,
			boolean link,
			int colspan) {

		htmlWriter.writeFormat (
			"<td");

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
