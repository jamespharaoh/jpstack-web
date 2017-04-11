package wbs.services.messagetemplate.model;

import java.util.function.Consumer;

import wbs.framework.logging.TaskLogger;

public
interface MessageTemplateFieldTypeObjectHelperMethods {

	MessageTemplateFieldTypeRec findOrCreate (
			TaskLogger parentTaskLogger,
			MessageTemplateEntryTypeRec messageTemplateEntryType,
			String code,
			Consumer <MessageTemplateFieldTypeRec> consumer);

}
