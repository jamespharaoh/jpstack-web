package wbs.console.forms;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.forms.FormField.UpdateResult;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("nullFormFieldUpdateHook")
public
class NullFormFieldUpdateHook <Container, Generic, Native>
	implements FormFieldUpdateHook <Container, Generic, Native> {

	@Override
	public
	void onUpdate (
			@NonNull TaskLogger parentTaskLogger,
			UpdateResult <Generic, Native> updateResult,
			Container container,
			Record <?> linkObject,
			Optional <Object> objectRef,
			Optional <String> objectType) {

		// do nothing

	}

}
