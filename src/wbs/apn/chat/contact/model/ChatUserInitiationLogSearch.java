package wbs.apn.chat.contact.model;

import java.io.Serializable;
import java.util.Collection;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.utils.time.TextualInterval;

@Accessors (fluent = true)
@Data
public
class ChatUserInitiationLogSearch
	implements Serializable {

	Long chatId;

	TextualInterval timestamp;

	ChatUserInitiationReason reason;

	Long monitorUserId;

	boolean filter;

	Collection <Long> filterChatIds;

}
