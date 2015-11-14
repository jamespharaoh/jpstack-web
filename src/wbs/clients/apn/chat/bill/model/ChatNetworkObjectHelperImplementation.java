package wbs.clients.apn.chat.bill.model;

import javax.inject.Inject;
import javax.inject.Provider;

import com.google.common.base.Optional;

import wbs.clients.apn.chat.user.core.model.ChatUserRec;

public
class ChatNetworkObjectHelperImplementation
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