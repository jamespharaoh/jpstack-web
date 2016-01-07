package wbs.console.forms;

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

	String name ();
	String label ();

	Set<ScriptRef> scriptRefs ();

	void init (
			String fieldSetName);

	void renderTableCellList (
			FormatWriter out,
			Container object,
			boolean link,
			int colspan);

	void renderTableCellProperties (
			FormatWriter out,
			Container object);

	void renderFormRow (
			FormatWriter out,
			Container object,
			Optional<String> error);

	void renderFormReset (
			FormatWriter out,
			String indent,
			Container container);

	void renderCsvRow (
			FormatWriter out,
			Container object);

	UpdateResult<Generic,Native> update (
			Container container);

	void runUpdateHook (
			UpdateResult<Generic,Native> updateResult,
			Container container,
			PermanentRecord<?> linkObject,
			Optional<Object> objectRef,
			Optional<String> objectType);

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
