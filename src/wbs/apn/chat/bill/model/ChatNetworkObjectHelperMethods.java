package wbs.apn.chat.bill.model;

import com.google.common.base.Optional;

import wbs.apn.chat.bill.model.ChatNetworkRec;
import wbs.apn.chat.user.core.model.ChatUserRec;

public
interface ChatNetworkObjectHelperMethods {

	Optional<ChatNetworkRec> forUser (
		ChatUserRec chatUser);

}