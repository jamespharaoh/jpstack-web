package wbs.platform.media.console;

import static wbs.utils.etc.Misc.doNothing;
import static wbs.utils.etc.Misc.successResult;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.moreThanZero;
import static wbs.utils.string.StringUtils.stringFormat;

import java.io.IOException;
import java.util.Map;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.IOUtils;

import wbs.console.forms.FormFieldRenderer;
import wbs.console.forms.FormFieldSubmission;
import wbs.console.forms.FormType;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.platform.media.logic.MediaLogic;
import wbs.platform.media.model.MediaRec;
import wbs.utils.io.RuntimeIoException;
import wbs.utils.string.FormatWriter;

import fj.data.Either;

@PrototypeComponent ("imageFormFieldRenderer")
@Accessors (fluent = true)
public
class ImageFormFieldRenderer <Container>
	implements FormFieldRenderer <Container, MediaRec> {

	// singleton dependencies

	@SingletonDependency
	MediaConsoleLogic mediaConsoleLogic;

	@SingletonDependency
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
			@NonNull Map <String, Object> hints,
			@NonNull Optional <MediaRec> interfaceValue,
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
			@NonNull Map <String, Object> hints,
			@NonNull Optional <MediaRec> interfaceValue,
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
			@NonNull Container container,
			@NonNull Optional<MediaRec> interfaceValue,
			@NonNull FormType formType,
			@NonNull String formName) {

		javascriptWriter.writeLineFormat (
			"$(\"#%j-%j\").replaceWith (",
			formName,
			name);

		javascriptWriter.writeLineFormat (
			"\t$(\"#%j-%j\").clone (true));",
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

		mediaConsoleLogic.writeMediaThumb32 (
			htmlWriter,
			interfaceValue.get ());

		if (showFilename) {

			htmlWriter.writeLineFormat (
				"%h",
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

		mediaConsoleLogic.writeMediaThumb100 (
			htmlWriter,
			interfaceValue.get ());

		if (showFilename) {

			htmlWriter.writeLineFormat (
				"<br>");

			htmlWriter.writeLineFormat (
				"%h (%h bytes)",
				interfaceValue.get ().getFilename (),
				integerToDecimalString (
					interfaceValue.get ().getContent ().getData ().length));

		}

	}

}
