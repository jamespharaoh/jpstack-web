package wbs.platform.console.forms;

import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.Html;
import wbs.platform.console.request.ConsoleRequestContext;

@PrototypeComponent ("textAreaFormFieldRenderer")
@Accessors (fluent = true)
public
class TextAreaFormFieldRenderer<Container>
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
	Integer rows;

	@Getter @Setter
	Integer cols;

	@Getter @Setter
	String charCountFunction;

	@Getter @Setter
	String charCountData;

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
				"<td>%s</td>\n",
				interfaceToHtml (
					container,
					interfaceValue,
					link)));

	}

	@Override
	public
	void renderTableCellProperties (
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

		List<String> errors =
			new ArrayList<String> ();

		if (formValuePresent ()) {

			interfaceValue =
				formToInterface (
					errors);

			if (! errors.isEmpty ())
				throw new RuntimeException ();

		}

		out.write (
			stringFormat (
				"<tr>\n",
				"<th>%h</th>\n",
				label ()));

		renderTableCellProperties (
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
				"<textarea",
				" id=\"field_%h\"",
				name (),
				" rows=\"%h\"",
				rows (),
				" cols=\"%h\"",
				cols (),
				" name=\"%h\"",
				name ()));

		if (charCountFunction != null) {

			String onchange =
				stringFormat (
					"%s (this, document.getElementById ('%j'), %s);",
					charCountFunction,
					"chars_" + name (),
					ifNull (
						charCountData,
						"undefined"));

			out.write (
				stringFormat (
					" onkeyup=\"%h\"",
					onchange,
					" onfocus=\"%h\"",
					onchange));

		}

		out.write (
			stringFormat (
				">%h</textarea>",
				interfaceValue));

		if (charCountFunction != null) {

			out.write (
				stringFormat (
					"<br>\n"));

			out.write (
				stringFormat (
					"<span",
					" id=\"chars_%h\"",
					name (),
					">&nbsp;</span>"));

		}

	}

	@Override
	public
	boolean formValuePresent () {

		String paramString =
			requestContext.parameter (name ());

		return paramString != null;

	}

	@Override
	public
	String formToInterface (
			List<String> errors) {

		return requestContext.parameter (
			name ());

	}

	@Override
	public
	String interfaceToHtml (
			Container container,
			String interfaceValue,
			boolean link) {

		return Html.newlineToBr (
			stringFormat (
				"%h",
				interfaceValue));

	}

}
