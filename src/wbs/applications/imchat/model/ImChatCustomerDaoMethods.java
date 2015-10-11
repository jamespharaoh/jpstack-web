package wbs.applications.imchat.model;

import java.util.List;

public
interface ImChatCustomerDaoMethods {

	ImChatCustomerRec findByEmail (
			ImChatRec imChat,
			String email);

	List<Integer> searchIds (
			ImChatCustomerSearch imChatCustomerSearch);

}