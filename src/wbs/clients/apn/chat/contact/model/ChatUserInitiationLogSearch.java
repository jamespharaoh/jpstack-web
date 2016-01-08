package wbs.clients.apn.chat.contact.model;

import java.util.Collection;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
@Data
public
class ChatUserInitiationLogSearch {

	Integer chatId;

	String timestamp;

	ChatUserInitiationReason reason;

	Integer monitorUserId;

	boolean filter;

	Collection<Integer> filterChatIds;

}
