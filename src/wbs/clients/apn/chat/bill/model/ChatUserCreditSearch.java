package wbs.clients.apn.chat.bill.model;

import java.io.Serializable;
import java.util.Collection;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.utils.TextualInterval;

@Accessors (fluent = true)
@Data
public
class ChatUserCreditSearch
	implements Serializable {

	Long chatId;

	TextualInterval timestamp;

	boolean filter;

	Collection <Long> filterChatIds;

}
