package wbs.imchat.model;

import java.util.List;

import wbs.framework.logging.TaskLogger;

public
interface ImChatCustomerDaoMethods {

	ImChatCustomerRec findByEmail (
			ImChatRec imChat,
			String email);

	List <Long> searchIds (
			TaskLogger parentTaskLogger,
			ImChatCustomerSearch imChatCustomerSearch);

}