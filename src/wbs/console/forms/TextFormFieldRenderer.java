package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.isPresent;
import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.successResult;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;

import fj.data.Either;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.FormatWriter;

@PrototypeComponent ("textFormFieldRenderer")
@Accessors (fluent = true)
public
class TextFormFieldRenderer<Container>
	implements FormFieldRenderer<Container,String> {

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
			@NonNull FormatWriter out,
			@NonNull Container container,
			@NonNull Optional<String> interfaceValue,
			boolean link,
			int colspan) {

		out.writeFormat (
			"<td",

			colspan > 1
				? stringFormat (
					" colspan=\"%h\"",
					colspan)
				: "",

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
				link));

	}

	@Override
	public
	void renderTableCellProperties (
			@NonNull FormatWriter out,
			@NonNull Container container,
			@NonNull Optional<String> interfaceValue) {

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
			@NonNull Optional<String> interfaceValue) {

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
			@NonNull Optional<String> interfaceValue,
			@NonNull Optional<String> error) {

		out.writeFormat (
			"<tr>\n",
			"<th>%h</th>\n",
			label (),
			"<td>");

		renderFormInput (
			submission,
			out,
			container,
			interfaceValue);

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
			@NonNull FormatWriter out,
			@NonNull Container container,
			@NonNull Optional<String> interfaceValue) {

		out.writeFormat (
			"<input",
			" type=\"text\"",
			" size=\"%h\"",
			size (),
			" id=\"%h\"",
			name (),
			" name=\"%h\"",
			name (),
			" value=\"%h\"",
			formValuePresent (
					submission)
				? formValue (
					submission)
				: interfaceValue.or (
					""),
			">\n");

	}

	@Override
	public
	void renderFormReset (
			@NonNull FormatWriter javascriptWriter,
			@NonNull String indent,
			@NonNull Container container,
			@NonNull Optional<String> interfaceValue) {

		javascriptWriter.writeFormat (
			"%s$(\"#%j\").val (\"%j\");\n",
			indent,
			name,
			interfaceValue.or (""));

	}

	@Override
	public
	boolean formValuePresent (
			@NonNull FormFieldSubmission submission) {

		return submission.hasParameter (
			name ());

	}

	String formValue (
			@NonNull FormFieldSubmission submission) {

		return submission.parameter (
			name ());

	}

	@Override
	public
	Either<Optional<String>,String> formToInterface (
			@NonNull FormFieldSubmission submission) {

		String formValue =
			formValue (
				submission);

		if (

			nullable ()

			&& equal (
				formValue,
				"")

		) {

			return successResult (
				Optional.<String>absent ());

		}

		return successResult (
			Optional.fromNullable (
				formValue));

	}

	@Override
	public
	String interfaceToHtmlSimple (
			@NonNull Container container,
			@NonNull Optional<String> interfaceValue,
			boolean link) {

		return stringFormat (
			"%h",
			interfaceValue.or (""));

	}

	@Override
	public
	String interfaceToHtmlComplex (
			@NonNull Container container,
			@NonNull Optional<String> interfaceValue) {

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
