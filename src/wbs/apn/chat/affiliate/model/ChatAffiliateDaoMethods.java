package wbs.apn.chat.affiliate.model;

import java.util.List;

import wbs.apn.chat.affiliate.model.ChatAffiliateRec;
import wbs.apn.chat.core.model.ChatRec;

public
interface ChatAffiliateDaoMethods {

	List<ChatAffiliateRec> find (
			ChatRec chat);

}