package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.isPresent;
import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.successResult;

import java.io.IOException;

import javax.inject.Inject;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.IOUtils;

import com.google.common.base.Optional;

import fj.data.Either;

import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.FormatWriter;

@PrototypeComponent ("uploadFormFieldRenderer")
@Accessors (fluent = true)
public
class UploadFormFieldRenderer<Container>
	implements FormFieldRenderer<Container,FileUpload> {

	// dependencies

	@Inject
	ConsoleRequestContext requestContext;

	// properties

	@Getter @Setter
	String name;

	@Getter @Setter
	String label;

	// details

	@Getter
	boolean fileUpload = true;

	// implementation

	@Override
	public
	void renderTableCellList (
			@NonNull FormatWriter out,
			@NonNull Container container,
			@NonNull Optional<FileUpload> interfaceValue,
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
			@NonNull Optional<FileUpload> interfaceValue) {

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
			@NonNull Optional<FileUpload> interfaceValue) {

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
			@NonNull Optional<FileUpload> interfaceValue,
			@NonNull Optional<String> error) {

		out.writeFormat (
			"<tr>\n",
			"<th>%h</th>\n",
			label (),
			"<td>");

		renderFormInput (
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
			@NonNull FormatWriter out,
			@NonNull Container container,
			@NonNull Optional<FileUpload> interfaceValue) {

		out.writeFormat (
			"<input",
			" type=\"file\"",
			" name=\"%h\"",
			name (),
			">\n");

	}

	@Override
	public
	void renderFormReset (
			@NonNull FormatWriter javascriptWriter,
			@NonNull String indent,
			@NonNull Container container,
			@NonNull Optional<FileUpload> interfaceValue) {

		javascriptWriter.writeFormat (
			"%s$(\"#%j\").replaceWith (\n",
			indent,
			name);

		javascriptWriter.writeFormat (
			"%s\t$(\"#%j\").clone (true));\n",
			indent,
			name);

	}

	@Override
	public
	boolean formValuePresent () {

		if (! requestContext.isMultipart ())
			return false;

		FileItem fileItem =
			requestContext.fileItemFile (
				name ());

		return fileItem != null;

	}

	@SneakyThrows (IOException.class)
	FileUpload formValue () {

		FileItem fileItem =
			requestContext.fileItemFile (
				name ());

		if (fileItem == null)
			return null;

		byte[] data =
			IOUtils.toByteArray (
				fileItem.getInputStream ());

		return new FileUpload ()

			.name (
				fileItem.getName ())

			.data (
				data);

	}

	@Override
	public
	Either<Optional<FileUpload>,String> formToInterface () {

		return successResult (
			Optional.fromNullable (
				formValue ()));

	}

	@Override
	public
	String interfaceToHtmlSimple (
			@NonNull Container container,
			@NonNull Optional<FileUpload> interfaceValue,
			boolean link) {

		if (! interfaceValue.isPresent ()) {
			return "";
		}

		return stringFormat (
			"%h (%h bytes)",
			interfaceValue.get ().name (),
			interfaceValue.get ().data ().length);

	}

	@Override
	public
	String interfaceToHtmlComplex (
			@NonNull Container container,
			@NonNull Optional<FileUpload> interfaceValue) {

		return interfaceToHtmlSimple (
			container,
			interfaceValue,
			true);

	}

}
