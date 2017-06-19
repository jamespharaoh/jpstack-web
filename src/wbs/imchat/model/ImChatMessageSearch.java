package wbs.imchat.model;

import java.io.Serializable;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.utils.time.TextualInterval;

@Accessors (fluent = true)
@Data
public
class ImChatMessageSearch
	implements Serializable {

	Long imChatId;
	Long senderUserId;

	TextualInterval timestamp;

}
