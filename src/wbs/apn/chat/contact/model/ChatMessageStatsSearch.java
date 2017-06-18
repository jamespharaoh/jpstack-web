package wbs.apn.chat.contact.model;

import java.io.Serializable;
import java.util.Set;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.utils.time.TextualInterval;

@Accessors (fluent = true)
@Data
public
class ChatMessageStatsSearch
	implements Serializable {

	Set <Long> chatIds;
	Set <Long> senderUserIds;

	TextualInterval timestamp;

	Set <Long> filterChatIds;
	Set <Long> filterSenderUserIds;

}
