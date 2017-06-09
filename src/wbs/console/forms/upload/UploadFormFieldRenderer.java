package wbs.console.forms.upload;

import static wbs.utils.etc.Misc.doNothing;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.presentInstances;
import static wbs.utils.etc.ResultUtils.successResult;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.web.utils.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlColumnSpanAttribute;
import static wbs.web.utils.HtmlStyleUtils.htmlStyleRuleEntry;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellOpen;

import java.io.IOException;
import java.util.Map;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.IOUtils;

import wbs.console.forms.types.FormFieldRenderer;
import wbs.console.forms.types.FormFieldSubmission;
import wbs.console.forms.types.FormType;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.io.RuntimeIoException;
import wbs.utils.string.FormatWriter;

import fj.data.Either;

@PrototypeComponent ("uploadFormFieldRenderer")
@Accessors (fluent = true)
public
class UploadFormFieldRenderer <Container>
	implements FormFieldRenderer <Container, FileUpload> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

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
			@NonNull FormatWriter formatWriter,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <FileUpload> interfaceValue,
			@NonNull FormType formType,
			@NonNull String formName) {

		doNothing ();

	}

	@Override
	public
	void renderFormInput (
			@NonNull Transaction parentTransaction,
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter formatWriter,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <FileUpload> interfaceValue,
			@NonNull FormType formType,
			@NonNull String formName) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderFormInput");

		) {

			formatWriter.writeLineFormat (
				"<input",
				" type=\"file\"",
				" name=\"%h.%h\"",
				formName,
				name (),
				">");

		}

	}

	@Override
	public
	void renderFormReset (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter javascriptWriter,
			@NonNull Container container,
			@NonNull Optional <FileUpload> interfaceValue,
			@NonNull String formName) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderFormReset");

		) {

			javascriptWriter.writeLineFormat (
				"$(\"%j\").replaceWith (",
				stringFormat (
					"#%s\\.%s",
					formName,
					name));

			javascriptWriter.writeLineFormat (
				"\t$(\"%j\").clone (true));",
				stringFormat (
					"#%s\\.%s",
					formName,
					name));

		}

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
					"%s.%s",
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
					"%s.%s",
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
	Either <Optional <FileUpload>, String> formToInterface (
			@NonNull Transaction parentTransaction,
			@NonNull FormFieldSubmission submission,
			@NonNull String formName) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"formToInterface");

		) {

			return successResult (
				Optional.fromNullable (
					formValue (
						submission,
						formName)));

		}

	}

	@Override
	public
	void renderHtmlSimple (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <FileUpload> interfaceValue,
			boolean link) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlSimple");

		) {

			if (interfaceValue.isPresent ()) {

				htmlWriter.writeFormat (
					"%h (%h bytes)",
					interfaceValue.get ().name (),
					integerToDecimalString (
						interfaceValue.get ().data ().length));

			}

		}

	}

	@Override
	public
	void renderHtmlTableCellList (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <FileUpload> interfaceValue,
			@NonNull Boolean link,
			@NonNull Long colspan) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlTableCellList");

		) {

			htmlTableCellOpen (
				formatWriter,
				htmlStyleRuleEntry (
					"text-align",
					listAlign ().name ()),
				htmlColumnSpanAttribute (
					colspan),
				htmlClassAttribute (
					presentInstances (
						htmlClass (
							interfaceValue))));

			renderHtmlSimple (
				transaction,
				formatWriter,
				container,
				hints,
				interfaceValue,
				link);

			htmlTableCellClose (
				formatWriter);

		}

	}

	@Override
	public
	void renderHtmlTableCellProperties (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <FileUpload> interfaceValue,
			@NonNull Boolean link,
			@NonNull Long colspan) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlTableCellProperties");

		) {

			htmlTableCellOpen (
				formatWriter,
				htmlStyleRuleEntry (
					"text-align",
					propertiesAlign ().name ()),
				htmlColumnSpanAttribute (
					colspan));

			renderHtmlSimple (
				transaction,
				formatWriter,
				container,
				hints,
				interfaceValue,
				link);

			htmlTableCellClose (
				formatWriter);

		}

	}

}
