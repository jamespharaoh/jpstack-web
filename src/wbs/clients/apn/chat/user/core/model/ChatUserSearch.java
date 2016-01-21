package wbs.clients.apn.chat.user.core.model;

import java.io.Serializable;

import lombok.Data;
import lombok.experimental.Accessors;

import org.joda.time.Interval;

@Accessors (fluent = true)
@Data
public
class ChatUserSearch
	implements Serializable {

	Integer chatId;

	Interval lastJoin;

	Gender gender;
	Orient orient;

	Boolean blockAll;
	Boolean deleted;

}
