package wbs.imchat.model;

import java.util.List;

import wbs.imchat.model.ImChatCustomerRec;
import wbs.imchat.model.ImChatRec;

public
interface ImChatCustomerDaoMethods {

	ImChatCustomerRec findByEmail (
			ImChatRec imChat,
			String email);

	List <Long> searchIds (
			ImChatCustomerSearch imChatCustomerSearch);

}