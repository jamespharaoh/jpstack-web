package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.PrintWriter;
import java.util.List;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;

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
			PrintWriter out,
			Container container,
			String interfaceValue,
			boolean link) {

		out.write (
			stringFormat (
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
					link)));

	}

	@Override
	public
	void renderTableCellProperties (
			PrintWriter out,
			Container container,
			String interfaceValue) {

		out.write (
			stringFormat (
				"<td>%s</td>\n",
				interfaceToHtmlComplex (
					container,
					interfaceValue)));

	}

	@Override
	public
	void renderTableRow (
			PrintWriter out,
			Container container,
			String interfaceValue) {

		out.write (
			stringFormat (
				"<tr>\n",
				"<th>%h</th>\n",
				label ()));

		renderTableCellProperties (
			out,
			container,
			interfaceValue);

		out.write (
			stringFormat (
				"</tr>\n"));

	}

	@Override
	public
	void renderFormRow (
			PrintWriter out,
			Container container,
			String interfaceValue) {

		out.write (
			stringFormat (
				"<tr>\n",
				"<th>%h</th>\n",
				label (),
				"<td>"));

		renderFormInput (
			out,
			container,
			interfaceValue);

		out.write (
			stringFormat (
				"</td>\n",
				"</tr>\n"));

	}

	@Override
	public
	void renderFormInput (
			PrintWriter out,
			Container container,
			String interfaceValue) {

		out.write (
			stringFormat (
				"<input",
				" type=\"text\"",
				" size=\"%h\"",
				size (),
				" name=\"%h\"",
				name (),
				" value=\"%h\"",
				formValuePresent ()
					? formValue ()
					: interfaceValue,
				">\n"));

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
			interfaceValue);

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
