package wbs.apn.chat.bill.model;

import org.joda.time.LocalDate;

import wbs.framework.database.Transaction;

import wbs.apn.chat.user.core.model.ChatUserRec;

public
interface ChatUserSpendDaoMethods {

	ChatUserSpendRec findByDate (
			Transaction parentTransaction,
			ChatUserRec chatUser,
			LocalDate date);

}