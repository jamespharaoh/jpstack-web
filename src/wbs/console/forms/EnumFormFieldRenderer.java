package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.camelToSpaces;
import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.toEnum;
import static wbs.framework.utils.etc.Misc.toStringNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.console.helper.EnumConsoleHelper;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.FormatWriter;

@Accessors (fluent = true)
@PrototypeComponent ("enumFormFieldRenderer")
public
class EnumFormFieldRenderer<Container,Interface extends Enum<Interface>>
	implements FormFieldRenderer<Container,Interface> {

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
	EnumConsoleHelper<Interface> enumConsoleHelper;

	// details

	@Getter
	boolean fileUpload = false;

	// implementation

	@Override
	public
	void renderTableCellList (
			FormatWriter out,
			Container container,
			Interface interfaceValue,
			boolean link) {

		out.writeFormat (
			"<td>%h</td>\n",
			camelToSpaces (
				toStringNull (
					interfaceValue)));

	}

	@Override
	public
	void renderTableCellProperties (
			FormatWriter out,
			Container container,
			Interface interfaceValue) {

		out.writeFormat (
			"<td>%h</td>\n",
			camelToSpaces (
				toStringNull (
					interfaceValue)));

	}

	@Override
	public
	void renderTableRow (
			FormatWriter out,
			Container container,
			Interface interfaceValue) {

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
			Interface interfaceValue) {

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
			Interface interfaceValue) {

		List<String> errors =
			new ArrayList<String> ();

		String selectedValue =
			formValuePresent ()
				? requestContext.parameter (
					name ())
				: interfaceValue != null
					? interfaceValue.toString ()
					: "null";

		if (! errors.isEmpty ())
			throw new RuntimeException ();

		if (nullable) {

			out.writeFormat (
				"%s",
				enumConsoleHelper.selectNull (
					name (),
					selectedValue,
					"none"));

		} else {

			out.writeFormat (
				"%s",
				enumConsoleHelper.select (
					name (),
					selectedValue));

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
	Interface formToInterface (
			List<String> errors) {

		return toEnum (
			enumConsoleHelper.enumClass (),
			requestContext.parameter (
				name ()));

	}

	@Override
	public
	String interfaceToHtmlSimple (
			Container container,
			Interface interfaceValue,
			boolean link) {

		return stringFormat (
			"%h",
			interfaceValue.toString ());

	}

	@Override
	public
	String interfaceToHtmlComplex (
			Container container,
			Interface interfaceValue) {

		return interfaceToHtmlSimple (
			container,
			interfaceValue,
			true);

	}

}
