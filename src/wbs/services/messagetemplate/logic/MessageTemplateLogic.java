package wbs.services.messagetemplate.logic;

import wbs.framework.database.Transaction;

import wbs.platform.scaffold.model.SliceRec;

import wbs.services.messagetemplate.model.MessageTemplateDatabaseRec;

public
interface MessageTemplateLogic {

	MessageTemplateDatabaseRec readMessageTemplateDatabaseFromClasspath (
			Transaction parentTransaction,
			SliceRec slice,
			String resourceName);

}
