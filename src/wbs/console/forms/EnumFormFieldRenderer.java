package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.camelToSpaces;
import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.toEnum;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;

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
			@NonNull FormatWriter out,
			@NonNull Container container,
			@NonNull Optional<Interface> interfaceValue,
			boolean link,
			int colspan) {

		Optional<String> htmlClass =
			interfaceValue.isPresent ()
				? enumConsoleHelper.htmlClass (
					interfaceValue.get ())
				: Optional.<String>absent ();

		out.writeFormat (
			"<td",

			colspan > 1
				? stringFormat (
					" colspan=\"%h\"",
					colspan)
				: "",

			htmlClass.isPresent ()
				? stringFormat (
					" class=\"%h\"",
					htmlClass.get ())
				: "",

			">%h</td>\n",
			interfaceValue.isPresent ()
				? camelToSpaces (
					interfaceValue.get ().toString ())
				: "");

	}

	@Override
	public
	void renderTableCellProperties (
			@NonNull FormatWriter out,
			@NonNull Container container,
			@NonNull Optional<Interface> interfaceValue) {

		out.writeFormat (
			"<td>%h</td>\n",
			interfaceValue.isPresent ()
				? camelToSpaces (
					interfaceValue.get ().toString ())
				: "");

	}

	@Override
	public
	void renderTableRow (
			@NonNull FormatWriter out,
			@NonNull Container container,
			@NonNull Optional<Interface> interfaceValue) {

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
			@NonNull FormatWriter out,
			@NonNull Container container,
			@NonNull Optional<Interface> interfaceValue) {

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
			@NonNull FormatWriter out,
			@NonNull Container container,
			@NonNull Optional<Interface> interfaceValue) {

		List<String> errors =
			new ArrayList<String> ();

		String selectedValue =
			formValuePresent ()
				? requestContext.parameter (
					name ())
				: interfaceValue.isPresent ()
					? interfaceValue.get ().toString ()
					: "null";

		if (! errors.isEmpty ()) {
			throw new RuntimeException ();
		}

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
	void renderFormReset (
			@NonNull FormatWriter javascriptWriter,
			@NonNull String indent,
			@NonNull Container container,
			@NonNull Optional<Interface> interfaceValue) {

		javascriptWriter.writeFormat (
			"%s$(\"#%j\").val (\"null\");\n",
			indent,
			name);

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
	Optional<Interface> formToInterface (
			@NonNull List<String> errors) {

		return Optional.fromNullable (
			toEnum (
				enumConsoleHelper.enumClass (),
				requestContext.parameter (
					name ())));

	}

	@Override
	public
	String interfaceToHtmlSimple (
			@NonNull Container container,
			@NonNull Optional<Interface> interfaceValue,
			boolean link) {

		return stringFormat (
			"%h",
			interfaceValue.isPresent ()
				? interfaceValue.get ().toString ()
				: "");

	}

	@Override
	public
	String interfaceToHtmlComplex (
			@NonNull Container container,
			@NonNull Optional<Interface> interfaceValue) {

		return interfaceToHtmlSimple (
			container,
			interfaceValue,
			true);

	}

}
