package wbs.console.forms;

import wbs.console.forms.FormField.UpdateResult;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.Record;

@PrototypeComponent ("nullFormFieldUpdateHook")
public
class NullFormFieldUpdateHook<Container,Generic,Native>
	implements FormFieldUpdateHook<Container,Generic,Native> {

	@Override
	public
	void onUpdate (
			UpdateResult<Generic,Native> updateResult,
			Container container,
			Record<?> linkObject,
			Object objectRef,
			String objectType) {

		// do nothing

	}

}
