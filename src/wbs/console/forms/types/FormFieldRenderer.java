package wbs.console.forms.types;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;

import java.util.Map;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.database.Transaction;

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
			Map <String, Object> hints,
			Optional <Interface> interfaceValue,
			FormType formType,
			String formName);

	void renderFormInput (
			Transaction parentTransaction,
			FormFieldSubmission submission,
			FormatWriter htmlWriter,
			Container container,
			Map <String, Object> hints,
			Optional <Interface> interfaceValue,
			FormType formType,
			String formName);

	void renderFormReset (
			Transaction parentTransaction,
			FormatWriter htmlWriter,
			Container container,
			Optional <Interface> interfaceValue,
			String formName);

	void renderHtmlTableCellList (
			Transaction parentTransaction,
			FormatWriter htmlWriter,
			Container container,
			Map <String, Object> hints,
			Optional <Interface> interfaceValue,
			Boolean link,
			Long colspan);

	void renderHtmlTableCellProperties (
			Transaction parentTransaction,
			FormatWriter htmlWriter,
			Container container,
			Map <String, Object> hints,
			Optional <Interface> interfaceValue,
			Boolean link,
			Long colspan);

	void renderHtmlSimple (
			Transaction parentTransaction,
			FormatWriter htmlWriter,
			Container container,
			Map <String, Object> hints,
			Optional <Interface> interfaceValue,
			boolean link);

	default
	void renderHtmlComplex (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <Interface> interfaceValue) {

		renderHtmlSimple (
			parentTransaction,
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
	Either <Optional <Interface>, String> formToInterface (
			@NonNull Transaction parentTransaction,
			@NonNull FormFieldSubmission submission,
			@NonNull String formName) {

		throw new UnsupportedOperationException ();

	}

	default
	Optional <String> htmlClass (
			@NonNull Optional <Interface> interfaceValue) {

		return optionalAbsent ();

	}

}
