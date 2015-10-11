package wbs.clients.apn.chat.affiliate.model;

import java.util.List;

import wbs.clients.apn.chat.core.model.ChatRec;

public
interface ChatAffiliateDaoMethods {

	List<ChatAffiliateRec> find (
			ChatRec chat);

}