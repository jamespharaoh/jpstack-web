package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.doNothing;

import java.util.Map;
import java.util.Set;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;

import wbs.console.html.ScriptRef;
import wbs.framework.entity.record.PermanentRecord;
import wbs.framework.utils.etc.FormatWriter;

public
interface FormField<Container,Generic,Native,Interface> {

	final static
	int defaultSize = 48;

	Boolean fileUpload ();
	Boolean virtual ();
	Boolean large ();

	default
	String name () {

		throw new UnsupportedOperationException ();

	}

	default
	String label () {

		throw new UnsupportedOperationException ();

	}

	Set<ScriptRef> scriptRefs ();

	default
	boolean canView (
			@NonNull Container container,
			@NonNull Map<String,Object> hints) {

		return true;

	}

	default
	void init (
			String fieldSetName) {

		doNothing ();

	}

	default
	void renderTableCellList (
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			boolean link,
			int colspan) {

		doNothing ();

	}

	default
	void renderTableCellProperties (
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map<String,Object> hints) {

		doNothing ();

	}

	default
	void renderFormAlwaysHidden (
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
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
			@NonNull String indent,
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull FormType formType,
			@NonNull String formName) {

		doNothing ();

	}

	default
	void renderCsvRow (
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map<String,Object> hints) {

		doNothing ();

	}

	default
	void implicit (
			@NonNull Container container) {

		doNothing ();

	}

	default
	UpdateResult<Generic,Native> update (
			@NonNull FormFieldSubmission submission,
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull String formName) {

		throw new UnsupportedOperationException ();

	}

	default
	void runUpdateHook (
			@NonNull UpdateResult<Generic,Native> updateResult,
			@NonNull Container container,
			@NonNull PermanentRecord<?> linkObject,
			@NonNull Optional<Object> objectRef,
			@NonNull Optional<String> objectType) {

		doNothing ();

	}

	@Accessors (fluent = true)
	public static
	enum FormType {
		create,
		update,
		search,
		perform;
	}

	@Accessors (fluent = true)
	@Data
	public static
	class UpdateResult<Generic,Native> {

		Boolean updated;

		Optional<Generic> oldGenericValue;
		Optional<Generic> newGenericValue;

		Optional<Native> oldNativeValue;
		Optional<Native> newNativeValue;

		Optional<String> error;

	}

}
