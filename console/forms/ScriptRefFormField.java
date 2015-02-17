package wbs.platform.console.forms;

import java.io.PrintWriter;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.PermanentRecord;
import wbs.platform.console.html.ScriptRef;

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
	void renderTableCell (
			PrintWriter out,
			Object object,
			boolean link) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	void renderFormRow (
			PrintWriter out,
			Object object) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	void update (
			Object container,
			UpdateResult<Object,Object> updateResult) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	void runUpdateHook (
			UpdateResult<Object,Object> updateResult,
			Object container,
			PermanentRecord<?> linkObject,
			Object objectRef,
			String objectType) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	void renderCsvRow (
			PrintWriter out,
			Object object) {

		throw new UnsupportedOperationException ();

	}

}
