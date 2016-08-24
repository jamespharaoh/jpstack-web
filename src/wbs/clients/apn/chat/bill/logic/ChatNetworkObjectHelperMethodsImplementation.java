package wbs.clients.apn.chat.bill.logic;

import javax.inject.Inject;
import javax.inject.Provider;

import com.google.common.base.Optional;

import wbs.clients.apn.chat.bill.model.ChatNetworkObjectHelper;
import wbs.clients.apn.chat.bill.model.ChatNetworkObjectHelperMethods;
import wbs.clients.apn.chat.bill.model.ChatNetworkRec;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;

public
class ChatNetworkObjectHelperMethodsImplementation
	implements ChatNetworkObjectHelperMethods {

	@Inject
	Provider<ChatNetworkObjectHelper> chatNetworkHelper;

	@Override
	public
	Optional<ChatNetworkRec> forUser (
			ChatUserRec chatUser) {

		ChatNetworkRec chatNetwork =
			chatNetworkHelper.get ().find (
				chatUser.getChat (),
				chatUser.getNumber ().getNetwork ());

		return Optional.fromNullable (
			chatNetwork);

	}

}