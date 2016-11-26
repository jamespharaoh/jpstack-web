package wbs.console.forms;

import static wbs.utils.etc.Misc.doNothing;

import java.util.Map;

import com.google.common.base.Optional;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.framework.entity.record.PermanentRecord;
import wbs.framework.logging.TaskLogger;

import wbs.utils.string.FormatWriter;

public
interface FormField <Container, Generic, Native, Interface>
	extends FormItem <Container> {

	final static
	int defaultSize = 48;

	Boolean fileUpload ();
	Boolean large ();

	@Override
	default
	boolean canView (
			@NonNull Container container,
			@NonNull Map <String, Object> hints) {

		return true;

	}

	default
	void renderTableCellList (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Boolean link,
			@NonNull Long columnSpan) {

		doNothing ();

	}

	default
	void renderTableCellProperties (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map <String, Object> hints) {

		doNothing ();

	}

	default
	void renderFormAlwaysHidden (
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull FormType formType,
			@NonNull String formName) {

		doNothing ();

	}

	default
	void renderFormTemporarilyHidden (
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull FormType formType,
			@NonNull String formName) {

		doNothing ();

	}

	default
	void renderFormRow (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<String> error,
			@NonNull FormType formType,
			@NonNull String formName) {

		doNothing ();

	}

	default
	void renderFormReset (
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull FormType formType,
			@NonNull String formName) {

		doNothing ();

	}

	default
	void renderCsvRow (
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map <String, Object> hints) {

		doNothing ();

	}

	default
	void implicit (
			@NonNull Container container) {

		doNothing ();

	}

	default
	UpdateResult <Generic, Native> update (
			@NonNull FormFieldSubmission submission,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull String formName) {

		throw new UnsupportedOperationException ();

	}

	default
	void runUpdateHook (
			@NonNull UpdateResult <Generic, Native> updateResult,
			@NonNull Container container,
			@NonNull PermanentRecord <?> linkObject,
			@NonNull Optional <Object> objectRef,
			@NonNull Optional <String> objectType) {

		doNothing ();

	}

	@Accessors (fluent = true)
	@Data
	public static
	class UpdateResult <Generic, Native> {

		Boolean updated;

		Optional <Generic> oldGenericValue;
		Optional <Generic> newGenericValue;

		Optional <Native> oldNativeValue;
		Optional <Native> newNativeValue;

		Optional <String> error;

	}

	// data

	public static
	enum Align {
		left,
		center,
		right;
	}

}
