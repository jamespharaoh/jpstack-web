package wbs.clients.apn.chat.contact.model;

import java.io.Serializable;
import java.util.Collection;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.utils.TextualInterval;

@Accessors (fluent = true)
@Data
public
class ChatUserInitiationLogSearch
	implements Serializable {

	Integer chatId;

	TextualInterval timestamp;

	ChatUserInitiationReason reason;

	Integer monitorUserId;

	boolean filter;

	Collection<Long> filterChatIds;

}
