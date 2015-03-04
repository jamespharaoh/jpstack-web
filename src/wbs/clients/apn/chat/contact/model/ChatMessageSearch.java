package wbs.clients.apn.chat.contact.model;

import java.util.HashSet;
import java.util.Set;

import lombok.Data;
import lombok.experimental.Accessors;

import org.joda.time.Instant;

@Accessors (fluent = true)
@Data
public
class ChatMessageSearch {

	Integer chatId;

	Integer fromUserId;
	Integer toUserId;

	Integer originalTextId;

	Instant timestampAfter;
	Instant timestampBefore;

	Boolean hasSender;

	Integer idGreaterThan;

	Integer deliveryId;
	Integer deliveryIdGreaterThan;

	ChatMessageMethod method;

	Set<ChatMessageStatus> statusIn =
		new HashSet<ChatMessageStatus> ();

	Order orderBy;

	public static
	enum Order {
		deliveryId;
	}

}
