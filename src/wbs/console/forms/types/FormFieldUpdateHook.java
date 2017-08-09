package wbs.console.forms.types;

import com.google.common.base.Optional;

import wbs.console.request.ConsoleRequestContext;

import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;

public
interface FormFieldUpdateHook <Container, Generic, Native> {

	void onUpdate (
			Transaction parentTransaction,
			ConsoleRequestContext requestContext,
			FormUpdateResult <Generic, Native> updateResult,
			Container container,
			Record <?> linkObject,
			Optional <Object> objectRef,
			Optional <String> objectType);

}
