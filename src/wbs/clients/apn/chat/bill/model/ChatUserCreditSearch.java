package wbs.clients.apn.chat.bill.model;

import java.util.Collection;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
@Data
public
class ChatUserCreditSearch {

	Integer chatId;

	String timestamp;

	boolean filter;
	Collection<Integer> filterChatIds;

}
