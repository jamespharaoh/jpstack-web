package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.emptyStringIfNull;
import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.FormatWriter;

@PrototypeComponent ("textFormFieldRenderer")
@Accessors (fluent = true)
public
class TextFormFieldRenderer<Container>
	implements FormFieldRenderer<Container,String> {

	// dependencies

	@Inject
	ConsoleRequestContext requestContext;

	// properties

	@Getter @Setter
	String name;

	@Getter @Setter
	String label;

	@Getter @Setter
	Boolean nullable;

	@Getter @Setter
	Integer size;

	@Getter @Setter
	Align align;

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

			"<td",

			"%s",
			align != null
				? stringFormat (
					" style=\"text-align: %h\"",
					align.toString ())
				: "",

			">%s</td>\n",
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

		out.writeFormat (
			"<tr>\n",
			"<th>%h</th>\n",
			label (),
			"<td>");

		renderFormInput (
			out,
			container,
			interfaceValue);

		out.writeFormat (
			"</td>\n",
			"</tr>\n");

	}

	@Override
	public
	void renderFormInput (
			FormatWriter out,
			Container container,
			String interfaceValue) {

		out.writeFormat (
			"<input",
			" type=\"text\"",
			" size=\"%h\"",
			size (),
			" name=\"%h\"",
			name (),
			" value=\"%h\"",
			formValuePresent ()
				? formValue ()
				: emptyStringIfNull (
					interfaceValue),
			">\n");

	}

	@Override
	public
	boolean formValuePresent () {

		String paramString =
			requestContext.parameter (
				name ());

		return paramString != null;

	}

	String formValue () {

		return requestContext.parameter (
			name ());

	}

	@Override
	public
	String formToInterface (
			List<String> errors) {

		String formValue =
			formValue ();

		if (

			nullable ()

			&& equal (
				formValue,
				"")

		) {
			return null;
		}

		return formValue;

	}

	@Override
	public
	String interfaceToHtmlSimple (
			Container container,
			String interfaceValue,
			boolean link) {

		return stringFormat (
			"%h",
			emptyStringIfNull (
				interfaceValue));

	}

	@Override
	public
	String interfaceToHtmlComplex (
			Container container,
			String interfaceValue) {

		return interfaceToHtmlSimple (
			container,
			interfaceValue,
			true);

	}

	// data

	public static
	enum Align {
		left,
		center,
		right;
	}

}
