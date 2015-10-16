package wbs.console.forms;

import java.util.List;

import wbs.framework.utils.etc.FormatWriter;

public
interface FormFieldRenderer<Container,Interface> {

	boolean fileUpload ();

	void renderTableCellProperties (
			FormatWriter out,
			Container container,
			Interface interfaceValue);

	void renderTableCellList (
			FormatWriter out,
			Container container,
			Interface interfaceValue,
			boolean link);

	void renderTableRow (
			FormatWriter out,
			Container container,
			Interface interfaceValue);

	void renderFormRow (
			FormatWriter out,
			Container container,
			Interface interfaceValue);

	void renderFormInput (
			FormatWriter out,
			Container container,
			Interface interfaceValue);

	boolean formValuePresent ();

	Interface formToInterface (
			List<String> errors);

	String interfaceToHtmlSimple (
			Container container,
			Interface interfaceValue,
			boolean link);

	String interfaceToHtmlComplex (
			Container container,
			Interface interfaceValue);

}
