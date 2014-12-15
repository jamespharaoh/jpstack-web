package wbs.platform.console.forms;

import static wbs.framework.utils.etc.Misc.booleanToString;
import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.stringToBoolean;

import java.io.PrintWriter;
import java.util.List;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.request.ConsoleRequestContext;

@Accessors (fluent = true)
@PrototypeComponent ("yesNoFormFieldRenderer")
public
class YesNoFormFieldRenderer<Container>
	implements FormFieldRenderer<Container,Boolean> {

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
	String yesLabel;

	@Getter @Setter
	String noLabel;

	@Override
	public
	void renderTableCell (
			PrintWriter out,
			Container container,
			Boolean interfaceValue,
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
			Boolean interfaceValue,
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
			Boolean interfaceValue) {

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
			Boolean interfaceValue) {

		out.write (
			stringFormat (
				"<select name=\"%h\">",
				name ()));

		if (interfaceValue == null) {

			out.write (
				stringFormat (

					"<option",
					" value=\"\"",
					" selected",
					"></option>\n",

					"<option",
					" value=\"yes\"",
					">%h</option>\n",
					yesLabel (),

					"<option",
					" value=\"no\"",
					">%h</option>\n",
					noLabel ()));

		} else if (interfaceValue == true) {

			out.write (
				stringFormat (

					"<option",
					" value=\"yes\"",
					" selected",
					">%h</option>\n",
					yesLabel (),

					"<option",
					" value=\"no\"",
					">%h</option>\n",
					noLabel ()));

		} else if (interfaceValue == false) {

			out.write (
				stringFormat (

					"<option",
					" value=\"yes\"",
					">%h</option>\n",
					yesLabel (),

					"<option",
					" value=\"no\"",
					" selected",
					">%h</option>\n",
					noLabel ()));

		} else {

			throw new RuntimeException ();

		}

		out.write (
			stringFormat (
				"</select>"));

	}

	@Override
	@SneakyThrows (FileUploadException.class)
	public
	boolean formValuePresent () {

		if (requestContext.isMultipart ()) {

			FileItem fileItem =
				requestContext.fileItem (
					name ());

			return fileItem != null;

		} else {

			String parameterValue =
				requestContext.parameter (
					name ());

			return parameterValue != null;

		}

	}

	@SneakyThrows (FileUploadException.class)
	String formValue () {

		if (requestContext.isMultipart ()) {

			FileItem fileItem =
				requestContext.fileItem (
					name ());

			return fileItem.getString ();

		} else {

			String parameterValue =
				requestContext.parameter (
					name ());

			return parameterValue;

		}

	}

	@Override
	public
	Boolean formToInterface (
			List<String> errors) {

		String formValue =
			formValue ();

		return stringToBoolean (
			formValue);

	}

	@Override
	public
	String interfaceToHtml (
			Container container,
			Boolean genericValue,
			boolean link) {

		return stringFormat (
			"%h",
			booleanToString (
				genericValue,
				yesLabel (),
				noLabel (),
				"-"));

	}

}
