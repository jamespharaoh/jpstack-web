package wbs.apn.chat.broadcast.model;

import java.util.List;

import wbs.framework.database.Transaction;

public
interface ChatBroadcastNumberDaoMethods {

	List <ChatBroadcastNumberRec> findAcceptedLimit (
			Transaction parentTransaction,
			ChatBroadcastRec chatBroadcast,
			Long limit);

}