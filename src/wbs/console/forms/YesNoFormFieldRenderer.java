package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.booleanToString;
import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.stringToBoolean;

import java.util.List;

import javax.inject.Inject;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;

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
			@NonNull FormatWriter out,
			@NonNull Container container,
			@NonNull Optional<Boolean> interfaceValue,
			boolean link,
			int colspan) {

		out.writeFormat (
			"<td",
			colspan > 1
				? stringFormat (
					" colspan=\"%h\"",
					colspan)
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
			@NonNull FormatWriter out,
			@NonNull Container container,
			@NonNull Optional<Boolean> interfaceValue) {

		out.writeFormat (
			"<td>%s</td>\n",
			interfaceToHtmlComplex (
				container,
				interfaceValue));

	}

	@Override
	public
	void renderTableRow (
			@NonNull FormatWriter out,
			@NonNull Container container,
			@NonNull Optional<Boolean> interfaceValue) {

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
			@NonNull Optional<Boolean> interfaceValue) {

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
			@NonNull Optional<Boolean> interfaceValue) {

		out.writeFormat (
			"<select name=\"%h\">",
			name ());

		if (! interfaceValue.isPresent ()) {

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

		} else if (interfaceValue.get () == true) {

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

		} else if (interfaceValue.get () == false) {

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
	void renderFormReset (
			@NonNull FormatWriter javascriptWriter,
			@NonNull String indent,
			@NonNull Container container,
			@NonNull Optional<Boolean> interfaceValue) {

		javascriptWriter.writeFormat (
			"%s$(\"#%j\").val (%s);\n",
			indent,
			name,
			booleanToString (
				interfaceValue.orNull (),
				"true",
				"false",
				""));

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
	Optional<Boolean> formToInterface (
			@NonNull List<String> errors) {

		String formValue =
			formValue ();

		return Optional.fromNullable (
			stringToBoolean (
				formValue));

	}

	@Override
	public
	String interfaceToHtmlSimple (
			@NonNull Container container,
			@NonNull Optional<Boolean> genericValue,
			boolean link) {

		return stringFormat (
			"%h",
			booleanToString (
				genericValue.orNull (),
				yesLabel (),
				noLabel (),
				"-"));

	}

	@Override
	public
	String interfaceToHtmlComplex (
			@NonNull Container container,
			@NonNull Optional<Boolean> genericValue) {

		return interfaceToHtmlSimple (
			container,
			genericValue,
			true);

	}

}
