package wbs.console.forms;

import static wbs.utils.etc.Misc.doNothing;

import java.util.Map;

import com.google.common.base.Optional;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.framework.database.Transaction;
import wbs.framework.entity.record.PermanentRecord;

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
			@NonNull Transaction parentTransaction,
			@NonNull Container container,
			@NonNull Map <String, Object> hints) {

		return true;

	}

	default
	void renderTableCellList (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Boolean link,
			@NonNull Long columnSpan) {

		doNothing ();

	}

	default
	void renderTableCellProperties (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map <String, Object> hints) {

		doNothing ();

	}

	default
	void renderFormAlwaysHidden (
			@NonNull Transaction parentTransaction,
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
			@NonNull Transaction parentTransaction,
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
			@NonNull Transaction parentTransaction,
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
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull FormType formType,
			@NonNull String formName) {

		doNothing ();

	}

	default
	void renderCsvRow (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map <String, Object> hints) {

		doNothing ();

	}

	default
	void implicit (
			@NonNull Transaction parentTransaction,
			@NonNull Container container) {

		doNothing ();

	}

	default
	void setDefault (
			@NonNull Transaction parentTransaction,
			@NonNull Container container) {

		doNothing ();

	}

	default
	UpdateResult <Generic, Native> update (
			@NonNull Transaction parentTransaction,
			@NonNull FormFieldSubmission submission,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull String formName) {

		throw new UnsupportedOperationException ();

	}

	default
	void runUpdateHook (
			@NonNull Transaction parentTransaction,
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
