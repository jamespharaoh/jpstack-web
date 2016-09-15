package wbs.apn.chat.bill.model;

import org.joda.time.LocalDate;

import wbs.apn.chat.bill.model.ChatUserSpendRec;
import wbs.apn.chat.user.core.model.ChatUserRec;

public
interface ChatUserSpendDaoMethods {

	ChatUserSpendRec findByDate (
			ChatUserRec chatUser,
			LocalDate date);

}