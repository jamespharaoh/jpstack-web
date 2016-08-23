package wbs.applications.imchat.model;

import java.io.Serializable;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.utils.TextualInterval;

@Accessors (fluent = true)
@Data
public
class ImChatMessageSearch
	implements Serializable {

	Long imChatId;

	TextualInterval timestamp;

}
