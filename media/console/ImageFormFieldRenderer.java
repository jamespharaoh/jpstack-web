package wbs.platform.media.console;

import static wbs.framework.utils.etc.Misc.doNothing;
import static wbs.framework.utils.etc.Misc.successResult;
import static wbs.framework.utils.etc.NumberUtils.moreThanZero;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.IOUtils;

import com.google.common.base.Optional;

import fj.data.Either;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.console.forms.FormField.FormType;
import wbs.console.forms.FormFieldRenderer;
import wbs.console.forms.FormFieldSubmission;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.FormatWriter;
import wbs.framework.utils.etc.RuntimeIoException;
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

	// properties

	@Getter @Setter
	String name;

	@Getter @Setter
	String label;

	@Getter @Setter
	Boolean nullable;

	@Getter @Setter
	Boolean showFilename;

	// details

	@Getter
	boolean fileUpload = true;

	// implementation

	@Override
	public
	void renderFormTemporarilyHidden (
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<MediaRec> interfaceValue,
			@NonNull FormType formType,
			@NonNull String formName) {

		doNothing ();

	}

	@Override
	public
	void renderFormInput (
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<MediaRec> interfaceValue,
			@NonNull FormType formType,
			@NonNull String formName) {

		if (interfaceValue.isPresent ()) {

			renderHtmlComplex (
				htmlWriter,
				container,
				hints,
				interfaceValue);

			htmlWriter.writeFormat (
				"<br>\n");

		}

		htmlWriter.writeFormat (
			"<input",
			" type=\"file\"",
			" name=\"%h-%h\"",
			formName,
			name (),
			"><br>\n");

		if (
			interfaceValue.isPresent ()
			&& nullable ()
		) {

			htmlWriter.writeFormat (
				"<input",
				" type=\"submit\"",
				" name=\"%h-%h-remove\"",
				formName,
				name (),
				" value=\"remove image\"",
				">\n");

		}

	}

	@Override
	public
	void renderFormReset (
			@NonNull FormatWriter javascriptWriter,
			@NonNull String indent,
			@NonNull Container container,
			@NonNull Optional<MediaRec> interfaceValue,
			@NonNull FormType formType,
			@NonNull String formName) {

		javascriptWriter.writeFormat (
			"%s$(\"#%j-%j\").replaceWith (\n",
			indent,
			formName,
			name);

		javascriptWriter.writeFormat (
			"%s\t$(\"#%j-%j\").clone (true));\n",
			indent,
			formName,
			name);

	}

	@Override
	public
	boolean formValuePresent (
			@NonNull FormFieldSubmission submission,
			@NonNull String formName) {

		return (

			submission.hasParameter (
				stringFormat (
					"%s-%s-remove",
					formName,
					name ()))

		) || (

			submission.multipart ()

			&& submission.hasFileItem (
				stringFormat (
					"%s-%s",
					formName,
					name ()))

			&& moreThanZero (
				submission.fileItem (
					stringFormat (
						"%s-%s",
						formName,
						name ())
				).getSize ())

		);

	}

	MediaRec formValue (
			@NonNull FormFieldSubmission submission,
			@NonNull String formName) {

		if (
			submission.hasParameter (
				stringFormat (
					"%s-%s-remove",
					formName,
					name ()))
		) {
			return null;
		}

		FileItem fileItem =
			submission.fileItem (
				stringFormat (
					"%s-%s",
					formName,
					name ()));

		try {

			byte[] data =
				IOUtils.toByteArray (
					fileItem.getInputStream ());

			return mediaLogic.createMediaFromImageRequired (
				data,
				"image/jpeg",
				fileItem.getName ());

		} catch (IOException exception) {

			throw new RuntimeIoException (
				exception);

		}

	}

	@Override
	public
	Either<Optional<MediaRec>,String> formToInterface (
			@NonNull FormFieldSubmission submission,
			@NonNull String formName) {

		return successResult (
			Optional.fromNullable (
				formValue (
					submission,
					formName)));

	}

	@Override
	public
	void renderHtmlSimple (
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<MediaRec> interfaceValue,
			boolean link) {

		if (! interfaceValue.isPresent ()) {
			return;
		}

		htmlWriter.writeFormat (
			"%s",
			mediaConsoleLogic.mediaThumb32 (
				interfaceValue.get ()));

		if (showFilename) {

			htmlWriter.writeFormat (
				"\n%h",
				interfaceValue.get ().getFilename ());

		}

	}

	@Override
	public
	void renderHtmlComplex (
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<MediaRec> interfaceValue) {

		if (! interfaceValue.isPresent ()) {
			return;
		}

		htmlWriter.writeFormat (
			"%s",
			mediaConsoleLogic.mediaThumb100 (
				interfaceValue.get ()));

		if (showFilename) {

			htmlWriter.writeFormat (
				"<br>\n%h (%h bytes)",
				interfaceValue.get ().getFilename (),
				interfaceValue.get ().getContent ().getData ().length);

		}

	}

}
