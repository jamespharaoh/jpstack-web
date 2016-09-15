package wbs.apn.chat.contact.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.joda.time.Instant;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
@Data
public
class ChatMessageSearch
	implements Serializable {

	Long chatId;

	Long fromUserId;
	Long toUserId;

	Long originalTextId;

	Instant timestampAfter;
	Instant timestampBefore;

	Boolean hasSender;

	Long idGreaterThan;

	Long deliveryId;
	Long deliveryIdGreaterThan;

	ChatMessageMethod method;

	Set<ChatMessageStatus> statusIn =
		new HashSet<ChatMessageStatus> ();

	Order orderBy;

	public static
	enum Order {
		deliveryId;
	}

}
