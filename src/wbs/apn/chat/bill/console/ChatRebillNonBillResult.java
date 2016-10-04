package wbs.apn.chat.bill.console;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.apn.chat.user.core.model.ChatUserRec;

@Accessors (fluent = true)
@Data
public
class ChatRebillNonBillResult {

	ChatUserRec chatUser;
	String reason;

}
