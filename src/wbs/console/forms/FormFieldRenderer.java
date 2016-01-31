package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Map;

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
			FormType formType);

	void renderFormInput (
			FormFieldSubmission submission,
			FormatWriter htmlWriter,
			Container container,
			Map<String,Object> hints,
			Optional<Interface> interfaceValue,
			FormType formType);

	void renderFormReset (
			FormatWriter htmlWriter,
			String indent,
			Container container,
			Optional<Interface> interfaceValue,
			FormType formType);

	boolean formValuePresent (
			FormFieldSubmission submission);

	Either<Optional<Interface>,String> formToInterface (
			FormFieldSubmission submission);

	default
	String interfaceToHtmlTableCell (
			Container container,
			Optional<Interface> interfaceValue,
			boolean link,
			int colspan) {

		return stringFormat (
			"<td",
			colspan != 1
				? stringFormat (
					" colspan=\"%h\"",
					colspan)
				: "",
			">%s</td>",
			interfaceToHtmlSimple (
				container,
				interfaceValue,
				link));

	}

	String interfaceToHtmlSimple (
			Container container,
			Optional<Interface> interfaceValue,
			boolean link);

	String interfaceToHtmlComplex (
			Container container,
			Optional<Interface> interfaceValue);

}
