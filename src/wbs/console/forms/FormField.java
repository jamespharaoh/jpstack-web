package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.doNothing;

import java.util.Set;

import lombok.Data;
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
	void init (
			String fieldSetName) {

		doNothing ();

	}

	default
	void renderTableCellList (
			FormatWriter out,
			Container object,
			boolean link,
			int colspan) {

		throw new UnsupportedOperationException ();

	}

	default
	void renderTableCellProperties (
			FormatWriter out,
			Container object) {

		throw new UnsupportedOperationException ();

	}

	default
	void renderFormRow (
			FormFieldSubmission submission,
			FormatWriter out,
			Container object,
			Optional<String> error,
			FormType formType) {

		throw new UnsupportedOperationException ();

	}

	default
	void renderFormReset (
			FormatWriter out,
			String indent,
			Container container,
			FormType formType) {

		throw new UnsupportedOperationException ();

	}

	default
	void renderCsvRow (
			FormatWriter out,
			Container object) {

		throw new UnsupportedOperationException ();

	}

	default
	UpdateResult<Generic,Native> update (
			FormFieldSubmission submission,
			Container container) {

		throw new UnsupportedOperationException ();

	}

	default
	void runUpdateHook (
			UpdateResult<Generic,Native> updateResult,
			Container container,
			PermanentRecord<?> linkObject,
			Optional<Object> objectRef,
			Optional<String> objectType) {

		throw new UnsupportedOperationException ();

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
