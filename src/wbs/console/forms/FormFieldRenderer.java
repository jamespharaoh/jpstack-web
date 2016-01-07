package wbs.console.forms;

import com.google.common.base.Optional;

import fj.data.Either;

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
			Optional<Interface> interfaceValue,
			Optional<String> error);

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

	Either<Optional<Interface>,String> formToInterface ();

	String interfaceToHtmlSimple (
			Container container,
			Optional<Interface> interfaceValue,
			boolean link);

	String interfaceToHtmlComplex (
			Container container,
			Optional<Interface> interfaceValue);

}
