package wbs.clients.apn.chat.bill.model;

import org.joda.time.LocalDate;

import wbs.clients.apn.chat.user.core.model.ChatUserRec;

public
interface ChatUserSpendDaoMethods {

	ChatUserSpendRec findByDate (
			ChatUserRec chatUser,
			LocalDate date);

}