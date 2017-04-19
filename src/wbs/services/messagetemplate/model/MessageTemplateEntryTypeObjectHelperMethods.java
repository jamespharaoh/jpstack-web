package wbs.services.messagetemplate.model;

import java.util.function.Consumer;

import wbs.framework.logging.TaskLogger;

public
interface MessageTemplateEntryTypeObjectHelperMethods {

	MessageTemplateEntryTypeRec findOrCreate (
			TaskLogger parentTaskLogger,
			MessageTemplateDatabaseRec messageTemplateDatabase,
			String code,
			Consumer <MessageTemplateEntryTypeRec> consumer);

}
