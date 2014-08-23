package wbs.platform.console.forms;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.PrintWriter;
import java.util.List;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.request.ConsoleRequestContext;

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

	// implementation

	@Override
	public
	void renderTableCell (
			PrintWriter out,
			Container container,
			String interfaceValue,
			boolean link) {

		out.write (
			stringFormat (
				"<td>%s</td>\n",
				interfaceToHtml (
					container,
					interfaceValue,
					link)));

	}

	@Override
	public
	void renderTableRow (
			PrintWriter out,
			Container container,
			String interfaceValue,
			boolean link) {

		out.write (
			stringFormat (
				"<tr>\n",
				"<th>%h</th>\n",
				label ()));

		renderTableCell (
			out,
			container,
			interfaceValue,
			link);

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
	String interfaceToHtml (
			Container container,
			String interfaceValue,
			boolean link) {

		return stringFormat (
			"%h",
			interfaceValue);

	}

}
