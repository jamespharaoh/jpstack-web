package wbs.console.forms.basic;

import static wbs.utils.etc.Misc.doNothing;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.forms.types.FormFieldUpdateHook;
import wbs.console.forms.types.FormUpdateResult;
import wbs.console.request.ConsoleRequestContext;

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
			@NonNull ConsoleRequestContext requestContext,
			@NonNull FormUpdateResult <Generic, Native> updateResult,
			@NonNull Container container,
			@NonNull Record <?> linkObject,
			@NonNull Optional <Object> objectRef,
			@NonNull Optional <String> objectType) {

		doNothing ();

	}

}
