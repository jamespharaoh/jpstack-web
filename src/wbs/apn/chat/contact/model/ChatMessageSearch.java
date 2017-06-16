package wbs.apn.chat.contact.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.utils.time.TextualInterval;

@Accessors (fluent = true)
@Data
public
class ChatMessageSearch
	implements Serializable {

	Set <Long> chatIds;
	Set <Long> senderUserIds;

	Long fromUserId;
	Long toUserId;

	Long originalTextId;

	TextualInterval timestamp;

	Boolean hasSender;

	Long idGreaterThan;

	Long deliveryId;
	Long deliveryIdGreaterThan;

	ChatMessageMethod method;

	Set <ChatMessageStatus> statusIn =
		new HashSet<> ();

	Boolean filter = false;

	Set <Long> filterChatIds;
	Set <Long> filterSenderUserIds;

	Order orderBy;

	public static
	enum Order {
		deliveryId;
	}

}
