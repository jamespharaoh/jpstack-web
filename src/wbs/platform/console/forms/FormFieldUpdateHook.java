package wbs.platform.console.forms;

import wbs.framework.record.Record;
import wbs.platform.console.forms.FormField.UpdateResult;

public
interface FormFieldUpdateHook<Container,Generic,Native> {

	void onUpdate (
			UpdateResult<Generic,Native> updateResult,
			Container container,
			Record<?> linkObject,
			Object objectRef,
			String objectType);

}
