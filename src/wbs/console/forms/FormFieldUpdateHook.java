package wbs.console.forms;

import com.google.common.base.Optional;

import wbs.console.forms.FormField.UpdateResult;

import wbs.framework.entity.record.Record;
import wbs.framework.logging.TaskLogger;

public
interface FormFieldUpdateHook <Container, Generic, Native> {

	void onUpdate (
			TaskLogger parentTaskLogger,
			UpdateResult <Generic, Native> updateResult,
			Container container,
			Record <?> linkObject,
			Optional <Object> objectRef,
			Optional <String> objectType);

}
