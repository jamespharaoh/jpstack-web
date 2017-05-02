package wbs.apn.chat.bill.model;

import com.google.common.base.Optional;

import wbs.framework.database.Transaction;

import wbs.apn.chat.user.core.model.ChatUserRec;

public
interface ChatNetworkObjectHelperMethods {

	Optional <ChatNetworkRec> forUser (
			Transaction parentTransaction,
			ChatUserRec chatUser);

}