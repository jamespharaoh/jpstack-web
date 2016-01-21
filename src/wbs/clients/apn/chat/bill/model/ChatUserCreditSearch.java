package wbs.clients.apn.chat.bill.model;

import java.io.Serializable;
import java.util.Collection;

import lombok.Data;
import lombok.experimental.Accessors;

import org.joda.time.Interval;

@Accessors (fluent = true)
@Data
public
class ChatUserCreditSearch
	implements Serializable {

	Integer chatId;

	Interval timestamp;

	boolean filter;
	Collection<Integer> filterChatIds;

}
