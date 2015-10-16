package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.booleanToString;
import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.stringToBoolean;

import java.util.List;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.FormatWriter;

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
			FormatWriter out,
			Container container,
			Boolean interfaceValue,
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
			Boolean interfaceValue) {

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
			Boolean interfaceValue) {

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
			Boolean interfaceValue) {

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
			Boolean interfaceValue) {

		out.writeFormat (
			"<select name=\"%h\">",
			name ());

		if (interfaceValue == null) {

			out.writeFormat (
				"<option",
				" value=\"\"",
				" selected",
				"></option>\n");

			out.writeFormat (
				"<option",
				" value=\"yes\"",
				">%h</option>\n",
				yesLabel ());

			out.writeFormat (
				"<option",
				" value=\"no\"",
				">%h</option>\n",
				noLabel ());

		} else if (interfaceValue == true) {

			out.writeFormat (
				"<option",
				" value=\"yes\"",
				" selected",
				">%h</option>\n",
				yesLabel ());

			out.writeFormat (
				"<option",
				" value=\"no\"",
				">%h</option>\n",
				noLabel ());

		} else if (interfaceValue == false) {

			out.writeFormat (
				"<option",
				" value=\"yes\"",
				">%h</option>\n",
				yesLabel ());

			out.writeFormat (
				"<option",
				" value=\"no\"",
				" selected",
				">%h</option>\n",
				noLabel ());

		} else {

			throw new RuntimeException ();

		}

		out.writeFormat (
			"</select>");

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
