package wbs.console.forms;

import java.util.Map;

import com.google.common.base.Optional;

import fj.data.Either;

import wbs.console.forms.FormField.FormType;
import wbs.framework.utils.etc.FormatWriter;

public
interface FormFieldRenderer<Container,Interface> {

	boolean fileUpload ();

	void renderTableCellProperties (
			FormatWriter out,
			Container container,
			Map<String,Object> hints,
			Optional<Interface> interfaceValue);

	void renderTableCellList (
			FormatWriter out,
			Container container,
			Optional<Interface> interfaceValue,
			boolean link,
			int colspan);

	void renderTableRow (
			FormatWriter out,
			Container container,
			Map<String,Object> hints,
			Optional<Interface> interfaceValue);

	void renderFormRow (
			FormFieldSubmission submission,
			FormatWriter out,
			Container container,
			Map<String,Object> hints,
			Optional<Interface> interfaceValue,
			Optional<String> error,
			FormType formType);

	void renderFormInput (
			FormFieldSubmission submission,
			FormatWriter out,
			Container container,
			Map<String,Object> hints,
			Optional<Interface> interfaceValue,
			FormType formType);

	void renderFormReset (
			FormatWriter out,
			String indent,
			Container container,
			Optional<Interface> interfaceValue,
			FormType formType);

	boolean formValuePresent (
			FormFieldSubmission submission);

	Either<Optional<Interface>,String> formToInterface (
			FormFieldSubmission submission);

	String interfaceToHtmlSimple (
			Container container,
			Optional<Interface> interfaceValue,
			boolean link);

	String interfaceToHtmlComplex (
			Container container,
			Optional<Interface> interfaceValue);

}
