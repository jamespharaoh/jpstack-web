package wbs.console.forms;

import static wbs.utils.etc.Misc.doNothing;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.forms.FormField.UpdateResult;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;

@PrototypeComponent ("nullFormFieldUpdateHook")
public
class NullFormFieldUpdateHook <Container, Generic, Native>
	implements FormFieldUpdateHook <Container, Generic, Native> {

	@Override
	public
	void onUpdate (
			@NonNull Transaction parentTransaction,
			@NonNull UpdateResult <Generic, Native> updateResult,
			@NonNull Container container,
			@NonNull Record <?> linkObject,
			@NonNull Optional <Object> objectRef,
			@NonNull Optional <String> objectType) {

		doNothing ();

	}

}
