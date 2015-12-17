package wbs.clients.apn.chat.user.core.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
@Data
public
class ChatUserSearch {

	Integer chatId;

	String lastJoin;

}
