package wbs.console.forms;

import wbs.console.forms.FormField.UpdateResult;
import wbs.framework.record.Record;

public
interface FormFieldUpdateHook<Container,Generic,Native> {

	void onUpdate (
			UpdateResult<Generic,Native> updateResult,
			Container container,
			Record<?> linkObject,
			Object objectRef,
			String objectType);

}
