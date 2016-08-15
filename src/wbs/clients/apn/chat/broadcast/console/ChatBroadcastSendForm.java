package wbs.clients.apn.chat.broadcast.console;

import lombok.Data;
import lombok.experimental.Accessors;

import org.joda.time.Interval;

import wbs.clients.apn.chat.user.core.model.Gender;
import wbs.clients.apn.chat.user.core.model.Orient;

@Accessors (fluent = true)
@Data
public
class ChatBroadcastSendForm {

	String numbers;

	Boolean search = true;

	Interval lastAction;

	Gender gender;
	Orient orient;

	Long categoryId;

	Boolean hasPicture;
	Boolean isAdult;

	Long minimumSpend;
	Long maximumSpend;

	Boolean includeBlocked = false;
	Boolean includeOptedOut = false;

	String fromUser;
	String prefix;
	String message;

}
