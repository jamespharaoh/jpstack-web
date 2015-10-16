package wbs.platform.media.console;

import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.IOUtils;

import wbs.console.forms.FormFieldRenderer;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.FormatWriter;
import wbs.platform.media.logic.MediaLogic;
import wbs.platform.media.model.MediaRec;

@PrototypeComponent ("imageFormFieldRenderer")
@Accessors (fluent = true)
public
class ImageFormFieldRenderer<Container>
	implements FormFieldRenderer<Container,MediaRec> {

	// dependencies

	@Inject
	MediaConsoleLogic mediaConsoleLogic;

	@Inject
	MediaLogic mediaLogic;

	@Inject
	ConsoleRequestContext requestContext;

	// properties

	@Getter @Setter
	String name;

	@Getter @Setter
	String label;

	@Getter @Setter
	Integer size;

	@Getter @Setter
	Boolean nullable;

	// details

	@Getter
	boolean fileUpload = true;

	// implementation

	@Override
	public
	void renderTableCellList (
			FormatWriter out,
			Container container,
			MediaRec interfaceValue,
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
			MediaRec interfaceValue) {

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
			MediaRec interfaceValue) {

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
			MediaRec interfaceValue) {

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
			MediaRec interfaceValue) {

		if (interfaceValue != null) {

			out.writeFormat (
				"%s<br>\n",
				interfaceToHtmlComplex (
					container,
					interfaceValue));

		}

		out.writeFormat (
			"<input",
			" type=\"file\"",
			" size=\"%h\"",
			size (),
			" name=\"%h\"",
			name (),
			" value=\"%h\"",
			formValuePresent ()
				? formValue ()
				: interfaceValue,
			"><br>\n");

		if (
			interfaceValue != null
			&& nullable ()
		) {

			out.writeFormat (
				"<input",
				" type=\"submit\"",
				" name=\"%h-remove\"",
				name (),
				" value=\"remove image\"",
				">\n");

		}

	}

	@Override
	public
	boolean formValuePresent () {

		if (
			isNotNull (
				requestContext.getForm (
					stringFormat (
						"%s-remove",
						name ())))
		) {
			return true;
		}

		if (! requestContext.isMultipart ())
			return false;

		FileItem fileItem =
			requestContext.fileItemFile (
				name ());

		return fileItem != null
			&& fileItem.getSize () > 0;

	}

	@SneakyThrows ({
		IOException.class
	})
	MediaRec formValue () {

		if (
			isNotNull (
				requestContext.getForm (
					stringFormat (
						"%s-remove",
						name ())))
		) {
			return null;
		}

		FileItem fileItem =
			requestContext.fileItemFile (
				name ());

		if (
			fileItem == null
			|| fileItem.getSize () == 0
		) {
			throw new IllegalStateException ();
		}

		byte[] data =
			IOUtils.toByteArray (
				fileItem.getInputStream ());

		return mediaLogic.createMediaFromImage (
			data,
			"image/jpeg",
			fileItem.getName ());

	}

	@Override
	public
	MediaRec formToInterface (
			List<String> errors) {

		return formValue ();

	}

	@Override
	public
	String interfaceToHtmlSimple (
			Container container,
			MediaRec interfaceValue,
			boolean link) {

		if (interfaceValue == null)
			return "";

		return stringFormat (
			"%s\n",
			mediaConsoleLogic.mediaThumb32 (
				interfaceValue),
			"%h",
			interfaceValue.getFilename ());

	}

	@Override
	public
	String interfaceToHtmlComplex (
			Container container,
			MediaRec interfaceValue) {

		if (interfaceValue == null)
			return "";

		return stringFormat (
			"%s<br>\n",
			mediaConsoleLogic.mediaThumb100 (
				interfaceValue),
			"%h (%h bytes)",
			interfaceValue.getFilename (),
			interfaceValue.getContent ().getData ().length);

	}

}
