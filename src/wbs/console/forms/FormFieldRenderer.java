package wbs.console.forms;

import java.util.List;

import com.google.common.base.Optional;

import wbs.framework.utils.etc.FormatWriter;

public
interface FormFieldRenderer<Container,Interface> {

	boolean fileUpload ();

	void renderTableCellProperties (
			FormatWriter out,
			Container container,
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
			Optional<Interface> interfaceValue);

	void renderFormRow (
			FormatWriter out,
			Container container,
			Optional<Interface> interfaceValue);

	void renderFormInput (
			FormatWriter out,
			Container container,
			Optional<Interface> interfaceValue);

	void renderFormReset (
			FormatWriter out,
			String indent,
			Container container,
			Optional<Interface> interfaceValue);

	boolean formValuePresent ();

	Optional<Interface> formToInterface (
			List<String> errors);

	String interfaceToHtmlSimple (
			Container container,
			Optional<Interface> interfaceValue,
			boolean link);

	String interfaceToHtmlComplex (
			Container container,
			Optional<Interface> interfaceValue);

}
