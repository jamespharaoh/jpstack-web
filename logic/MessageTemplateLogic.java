package wbs.services.messagetemplate.logic;

import wbs.platform.scaffold.model.SliceRec;
import wbs.services.messagetemplate.model.MessageTemplateDatabaseRec;

public
interface MessageTemplateLogic {

	MessageTemplateDatabaseRec readMessageTemplateDatabaseFromClasspath (
			SliceRec slice,
			String resourceName);

}
