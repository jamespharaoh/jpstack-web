package wbs.clients.apn.chat.user.core.model;

import java.io.Serializable;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
@Data
public
class ChatUserSearch
	implements Serializable {

	Integer chatId;

	String lastJoin;

}
