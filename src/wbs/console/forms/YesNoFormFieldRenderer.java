package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.booleanToString;
import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.stringToBoolean;

import java.io.PrintWriter;
import java.util.List;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;

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

	// details

	@Getter
	boolean fileUpload = false;

	// implementation

	@Override
	public
	void renderTableCellList (
			PrintWriter out,
			Container container,
			Boolean interfaceValue,
			boolean link) {

		out.write (
			stringFormat (
				"<td>%s</td>\n",
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
			Boolean interfaceValue) {

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
			Boolean interfaceValue) {

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
	public
	boolean formValuePresent () {

		String parameterValue =
			requestContext.parameter (
				name ());

		return parameterValue != null;

	}

	String formValue () {

		String parameterValue =
			requestContext.parameter (
				name ());

		return parameterValue;

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
	String interfaceToHtmlSimple (
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

	@Override
	public
	String interfaceToHtmlComplex (
			Container container,
			Boolean genericValue) {

		return interfaceToHtmlSimple (
			container,
			genericValue,
			true);

	}

}
