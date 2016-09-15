package wbs.apn.chat.broadcast.model;

import java.util.List;

import wbs.apn.chat.broadcast.model.ChatBroadcastNumberRec;
import wbs.apn.chat.broadcast.model.ChatBroadcastRec;

public
interface ChatBroadcastNumberDaoMethods {

	List<ChatBroadcastNumberRec> findAcceptedLimit (
			ChatBroadcastRec chatBroadcast,
			int limit);

}