package wbs.console.forms;

import com.google.common.base.Optional;

import wbs.console.forms.FormField.UpdateResult;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.entity.record.Record;

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
			Optional<Object> objectRef,
			Optional<String> objectType) {

		// do nothing

	}

}
