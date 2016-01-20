package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.camelToHyphen;
import static wbs.framework.utils.etc.Misc.camelToSpaces;
import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.in;
import static wbs.framework.utils.etc.Misc.isNotPresent;
import static wbs.framework.utils.etc.Misc.isPresent;
import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.successResult;
import static wbs.framework.utils.etc.Misc.toEnum;

import java.util.Map;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;

import fj.data.Either;

import wbs.console.forms.FormField.FormType;
import wbs.console.helper.EnumConsoleHelper;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.FormatWriter;

@Accessors (fluent = true)
@PrototypeComponent ("enumFormFieldRenderer")
public
class EnumFormFieldRenderer<Container,Interface extends Enum<Interface>>
	implements FormFieldRenderer<Container,Interface> {

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
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter out,
			@NonNull Container container,
			@NonNull Optional<Interface> interfaceValue,
			@NonNull Optional<String> error,
			@NonNull FormType formType) {

		out.writeFormat (
			"<tr>\n",
			"<th>%h</th>\n",
			label (),
			"<td>");

		renderFormInput (
			submission,
			out,
			container,
			interfaceValue,
			formType);

		if (
			isPresent (
				error)
		) {

			out.writeFormat (
				"<br>\n",
				"%h",
				error.get ());

		}

		out.writeFormat (
			"</td>\n",
			"</tr>\n");

	}

	@Override
	public
	void renderFormInput (
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Optional<Interface> interfaceValue,
			@NonNull FormType formType) {

		htmlWriter.writeFormat (
			"<select",
			" id=\"%h\"",
			name,
			" name=\"%h\"",
			name,
			">\n");

		if (

			nullable ()

			|| isNotPresent (
				interfaceValue)

			|| in (
				formType,
				FormType.create,
				FormType.search,
				FormType.update)

		) {

			htmlWriter.writeFormat (
				"<option",
				" value=\"none\"",
				interfaceValue.isPresent ()
					? ""
					: " selected",
				">&mdash;</option>\n");

		}

		for (
			Map.Entry<Interface,String> optionEntry
				: enumConsoleHelper.map ().entrySet ()
		) {

			Interface optionValue =
				optionEntry.getKey ();

			String optionLabel =
				optionEntry.getValue ();

			htmlWriter.writeFormat (
				"<option",
				" value=\"%h\"",
				camelToHyphen (
					optionValue.name ()),
				optionValue == interfaceValue.orNull ()
					? " selected"
					: "",
				">%h</option>\n",
				optionLabel);

		}

		htmlWriter.writeFormat (
			"</select>\n");

	}

	@Override
	public
	void renderFormReset (
			@NonNull FormatWriter javascriptWriter,
			@NonNull String indent,
			@NonNull Container container,
			@NonNull Optional<Interface> interfaceValue) {

		javascriptWriter.writeFormat (
			"%s$(\"#%j\").val (\"none\");\n",
			indent,
			name);

	}

	@Override
	public
	boolean formValuePresent (
			@NonNull FormFieldSubmission submission) {

		return submission.hasParameter (
			name ());

	}

	@Override
	public
	Either<Optional<Interface>,String> formToInterface (
			@NonNull FormFieldSubmission submission) {

		String parameterValue =
			submission.parameter (
				name ());

		if (
			equal (
				parameterValue,
				"none")
		) {

			return successResult (
				Optional.absent ());

		} else {

			return successResult (
				Optional.of (
					toEnum (
						enumConsoleHelper.enumClass (),
						submission.parameter (
							name ()))));

		}

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
