package wbs.platform.console.forms;

import java.io.PrintWriter;
import java.util.List;

public
interface FormFieldRenderer<Container,Interface> {

	boolean fileUpload ();

	void renderTableCell (
			PrintWriter out,
			Container container,
			Interface interfaceValue,
			boolean link);

	void renderTableRow (
			PrintWriter out,
			Container container,
			Interface interfaceValue,
			boolean link);

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

	String interfaceToHtml (
			Container container,
			Interface interfaceValue,
			boolean link);

}
