package wbs.clients.apn.chat.broadcast.model;

import java.util.List;

public
interface ChatBroadcastNumberDaoMethods {

	List<ChatBroadcastNumberRec> findAcceptedLimit (
			ChatBroadcastRec chatBroadcast,
			int limit);

}