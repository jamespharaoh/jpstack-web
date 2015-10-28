package wbs.console.forms;

import java.util.Set;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;

import wbs.console.html.ScriptRef;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.PermanentRecord;
import wbs.framework.utils.etc.FormatWriter;

@Accessors (fluent = true)
@PrototypeComponent ("scriptRefFormField")
public
class ScriptRefFormField
	implements FormField<Object,Object,Object,Object> {

	@Getter
	Boolean virtual = true;

	@Getter
	Boolean large;

	@Getter @Setter
	Set<ScriptRef> scriptRefs;

	@Override
	public
	void init (
			String fieldSetName) {

	}

	@Override
	public
	Boolean fileUpload () {
		return false;
	}

	@Override
	public
	String name () {
		return null;
	}

	@Override
	public
	String label () {
		return null;
	}

	@Override
	public
	void renderTableCellList (
			FormatWriter out,
			Object object,
			boolean link,
			int colspan) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	void renderTableCellProperties (
			FormatWriter out,
			Object object) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	void renderFormRow (
			FormatWriter out,
			Object object) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	void renderFormReset (
			@NonNull FormatWriter javascriptWriter,
			@NonNull String indent,
			@NonNull Object container) {

	}

	@Override
	public
	void update (
			@NonNull Object container,
			@NonNull UpdateResult<Object,Object> updateResult) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	void runUpdateHook (
			@NonNull UpdateResult<Object,Object> updateResult,
			@NonNull Object container,
			@NonNull PermanentRecord<?> linkObject,
			@NonNull Optional<Object> objectRef,
			@NonNull Optional<String> objectType) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	void renderCsvRow (
			@NonNull FormatWriter out,
			@NonNull Object object) {

		throw new UnsupportedOperationException ();

	}

}
