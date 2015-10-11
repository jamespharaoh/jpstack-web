package wbs.clients.apn.chat.user.core.model;

import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.number.core.model.NumberRec;

public
interface ChatUserObjectHelperMethods {

	ChatUserRec findOrCreate (
			ChatRec chat,
			NumberRec number);

	ChatUserRec findOrCreate (
			ChatRec chat,
			MessageRec message);

	ChatUserRec create (
			ChatRec chat,
			NumberRec number);

}