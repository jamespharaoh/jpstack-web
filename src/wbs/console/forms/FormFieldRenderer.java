package wbs.console.forms;

import java.io.PrintWriter;
import java.util.List;

public
interface FormFieldRenderer<Container,Interface> {

	boolean fileUpload ();

	void renderTableCellProperties (
			PrintWriter out,
			Container container,
			Interface interfaceValue);

	void renderTableCellList (
			PrintWriter out,
			Container container,
			Interface interfaceValue,
			boolean link);

	void renderTableRow (
			PrintWriter out,
			Container container,
			Interface interfaceValue);

	void renderFormRow (
			PrintWriter out,
			Container container,
			Interface interfaceValue);

	void renderFormInput (
			PrintWriter out,
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
