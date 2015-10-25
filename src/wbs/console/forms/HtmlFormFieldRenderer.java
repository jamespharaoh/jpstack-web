package wbs.console.forms;

import java.util.List;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.FormatWriter;

@PrototypeComponent ("htmlFormFieldRenderer")
@Accessors (fluent = true)
public
class HtmlFormFieldRenderer<Container>
	implements FormFieldRenderer<Container,String> {

	// dependencies

	@Inject
	ConsoleRequestContext requestContext;

	// properties

	@Getter @Setter
	String name;

	@Getter @Setter
	String label;

	// details

	@Getter
	boolean fileUpload = false;

	// implementation

	@Override
	public
	void renderTableCellList (
			FormatWriter out,
			Container container,
			String interfaceValue,
			boolean link) {

		out.writeFormat (
			"<td>%s</td>\n",
			interfaceToHtmlSimple (
				container,
				interfaceValue,
				link));

	}

	@Override
	public
	void renderTableCellProperties (
			FormatWriter out,
			Container container,
			String interfaceValue) {

		out.writeFormat (
			"<td>%s</td>\n",
			interfaceToHtmlComplex (
				container,
				interfaceValue));

	}

	@Override
	public
	void renderTableRow (
			FormatWriter out,
			Container container,
			String interfaceValue) {

		out.writeFormat (
			"<tr>\n",
			"<th>%h</th>\n",
			label ());

		renderTableCellProperties (
			out,
			container,
			interfaceValue);

		out.writeFormat (
			"</tr>\n");

	}

	@Override
	public
	void renderFormRow (
			FormatWriter out,
			Container container,
			String interfaceValue) {

		renderTableRow (
			out,
			container,
			interfaceValue);

	}

	@Override
	public
	void renderFormInput (
			FormatWriter out,
			Container container,
			String interfaceValue) {

		out.writeFormat (
			"%s",
			interfaceValue);

	}

	@Override
	public
	boolean formValuePresent () {

		return false;

	}

	@Override
	public
	String interfaceToHtmlSimple (
			Container container,
			String interfaceValue,
			boolean link) {

		return interfaceValue;

	}

	@Override
	public
	String interfaceToHtmlComplex (
			Container container,
			String interfaceValue) {

		return interfaceValue;

	}

	@Override
	public
	String formToInterface (
			List<String> errors) {

		throw new UnsupportedOperationException ();

	}

}
