package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.doNothing;
import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.successResult;

import java.io.IOException;
import java.util.Map;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.IOUtils;

import com.google.common.base.Optional;

import fj.data.Either;

import wbs.console.forms.FormField.FormType;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.FormatWriter;
import wbs.framework.utils.etc.RuntimeIoException;

@PrototypeComponent ("uploadFormFieldRenderer")
@Accessors (fluent = true)
public
class UploadFormFieldRenderer<Container>
	implements FormFieldRenderer<Container,FileUpload> {

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
	void renderFormTemporarilyHidden (
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<FileUpload> interfaceValue,
			@NonNull FormType formType,
			@NonNull String formName) {

		doNothing ();

	}

	@Override
	public
	void renderFormInput (
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter out,
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<FileUpload> interfaceValue,
			@NonNull FormType formType,
			@NonNull String formName) {

		out.writeFormat (
			"<input",
			" type=\"file\"",
			" name=\"%h-%h\"",
			formName,
			name (),
			">\n");

	}

	@Override
	public
	void renderFormReset (
			@NonNull FormatWriter javascriptWriter,
			@NonNull String indent,
			@NonNull Container container,
			@NonNull Optional<FileUpload> interfaceValue,
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

			submission.multipart ()

			&& submission.hasFileItem (
				stringFormat (
					"%s-%s",
					formName,
					name ()))

		);

	}

	FileUpload formValue (
			@NonNull FormFieldSubmission submission,
			@NonNull String formName) {

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

			return new FileUpload ()

				.name (
					fileItem.getName ())

				.data (
					data);

		} catch (IOException exception) {

			throw new RuntimeIoException (
				exception);

		}

	}

	@Override
	public
	Either<Optional<FileUpload>,String> formToInterface (
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
			@NonNull Optional<FileUpload> interfaceValue,
			boolean link) {

		if (interfaceValue.isPresent ()) {

			htmlWriter.writeFormat (
				"%h (%h bytes)",
				interfaceValue.get ().name (),
				interfaceValue.get ().data ().length);

		}

	}

}
