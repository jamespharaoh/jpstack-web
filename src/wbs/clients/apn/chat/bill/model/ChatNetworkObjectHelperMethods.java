package wbs.clients.apn.chat.bill.model;

import com.google.common.base.Optional;

import wbs.clients.apn.chat.user.core.model.ChatUserRec;

public
interface ChatNetworkObjectHelperMethods {

	Optional<ChatNetworkRec> forUser (
		ChatUserRec chatUser);

}