package wbs.apn.chat.affiliate.model;

import java.util.List;

import wbs.framework.database.Transaction;

import wbs.apn.chat.core.model.ChatRec;

public
interface ChatAffiliateDaoMethods {

	List <ChatAffiliateRec> find (
			Transaction parentTransaction,
			ChatRec chat);

}