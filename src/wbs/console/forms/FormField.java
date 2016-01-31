package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.doNothing;

import java.util.Map;
import java.util.Set;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;

import wbs.console.html.ScriptRef;
import wbs.framework.record.PermanentRecord;
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
			FormatWriter htmlWriter,
			Container container,
			Map<String,Object> hints) {

		doNothing ();

	}

	default
	void renderFormAlwaysHidden (
			FormFieldSubmission submission,
			FormatWriter htmlWriter,
			Container container,
			Map<String,Object> hints,
			FormType formType) {

		doNothing ();

	}

	default
	void renderFormTemporarilyHidden (
			FormFieldSubmission submission,
			FormatWriter htmlWriter,
			Container container,
			Map<String,Object> hints,
			FormType formType) {

		doNothing ();

	}

	default
	void renderFormRow (
			FormFieldSubmission submission,
			FormatWriter htmlWriter,
			Container container,
			Map<String,Object> hints,
			Optional<String> error,
			FormType formType) {

		doNothing ();

	}

	default
	void renderFormReset (
			@NonNull FormatWriter htmlWriter,
			@NonNull String indent,
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull FormType formType) {

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
	UpdateResult<Generic,Native> update (
			@NonNull FormFieldSubmission submission,
			@NonNull Container container,
			@NonNull Map<String,Object> hints) {

		throw new UnsupportedOperationException ();

	}

	default
	void runUpdateHook (
			UpdateResult<Generic,Native> updateResult,
			Container container,
			PermanentRecord<?> linkObject,
			Optional<Object> objectRef,
			Optional<String> objectType) {

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
