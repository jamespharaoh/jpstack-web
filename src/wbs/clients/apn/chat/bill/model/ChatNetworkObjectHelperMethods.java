package wbs.clients.apn.chat.bill.model;

import wbs.clients.apn.chat.user.core.model.ChatUserRec;

import com.google.common.base.Optional;

public
interface ChatNetworkObjectHelperMethods {

	Optional<ChatNetworkRec> forUser (
		ChatUserRec chatUser);

}