package wbs.console.forms;

import com.google.common.base.Optional;

import wbs.console.forms.FormField.UpdateResult;
import wbs.framework.entity.record.Record;

public
interface FormFieldUpdateHook <Container, Generic, Native> {

	void onUpdate (
			UpdateResult <Generic, Native> updateResult,
			Container container,
			Record <?> linkObject,
			Optional <Object> objectRef,
			Optional <String> objectType);

}
