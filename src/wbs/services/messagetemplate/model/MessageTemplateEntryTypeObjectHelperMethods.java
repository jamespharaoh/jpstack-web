package wbs.services.messagetemplate.model;

import wbs.framework.logging.TaskLogger;

public
interface MessageTemplateEntryTypeObjectHelperMethods {

	MessageTemplateEntryTypeRec findOrCreate (
			TaskLogger parentTaskLogger,
			MessageTemplateDatabaseRec messageTemplateDatabase,
			String code,
			String name,
			String description);

}
