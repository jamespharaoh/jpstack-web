package wbs.apn.chat.user.core.model;

import wbs.framework.database.Transaction;

import wbs.sms.message.core.model.MessageRec;
import wbs.sms.number.core.model.NumberRec;

import wbs.apn.chat.core.model.ChatRec;

public
interface ChatUserObjectHelperMethods {

	ChatUserRec findOrCreate (
			Transaction parentTransaction,
			ChatRec chat,
			NumberRec number);

	ChatUserRec findOrCreate (
			Transaction parentTransaction,
			ChatRec chat,
			MessageRec message);

	ChatUserRec create (
			Transaction parentTransaction,
			ChatRec chat,
			NumberRec number);

}