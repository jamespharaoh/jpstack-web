package wbs.apn.chat.user.core.model;

import wbs.framework.logging.TaskLogger;

import wbs.sms.message.core.model.MessageRec;
import wbs.sms.number.core.model.NumberRec;

import wbs.apn.chat.core.model.ChatRec;

public
interface ChatUserObjectHelperMethods {

	ChatUserRec findOrCreate (
			TaskLogger parentTaskLogger,
			ChatRec chat,
			NumberRec number);

	ChatUserRec findOrCreate (
			TaskLogger parentTaskLogger,
			ChatRec chat,
			MessageRec message);

	ChatUserRec create (
			TaskLogger parentTaskLogger,
			ChatRec chat,
			NumberRec number);

}