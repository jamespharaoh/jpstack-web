package wbs.services.messagetemplate.logic;

import wbs.framework.logging.TaskLogger;

import wbs.platform.scaffold.model.SliceRec;

import wbs.services.messagetemplate.model.MessageTemplateDatabaseRec;

public
interface MessageTemplateLogic {

	MessageTemplateDatabaseRec readMessageTemplateDatabaseFromClasspath (
			TaskLogger parentTaskLogger,
			SliceRec slice,
			String resourceName);

}
