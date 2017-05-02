package wbs.imchat.model;

import java.util.List;

import wbs.framework.database.Transaction;

public
interface ImChatCustomerDaoMethods {

	ImChatCustomerRec findByEmail (
			Transaction parentTransaction,
			ImChatRec imChat,
			String email);

	List <Long> searchIds (
			Transaction parentTransaction,
			ImChatCustomerSearch imChatCustomerSearch);

}