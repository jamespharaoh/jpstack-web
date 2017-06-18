package wbs.imchat.model;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.utils.time.TextualInterval;

@Accessors (fluent = true)
@Data
public
class ImChatMessageSearch {

	Long imChatId;
	Long senderUserId;

	TextualInterval timestamp;

}
