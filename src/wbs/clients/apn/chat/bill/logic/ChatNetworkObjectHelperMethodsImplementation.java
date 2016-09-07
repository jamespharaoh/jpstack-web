package wbs.clients.apn.chat.bill.logic;

import javax.inject.Provider;

import com.google.common.base.Optional;

import wbs.clients.apn.chat.bill.model.ChatNetworkObjectHelper;
import wbs.clients.apn.chat.bill.model.ChatNetworkObjectHelperMethods;
import wbs.clients.apn.chat.bill.model.ChatNetworkRec;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.component.annotations.PrototypeDependency;

public
class ChatNetworkObjectHelperMethodsImplementation
	implements ChatNetworkObjectHelperMethods {

	// prototype dependencies

	@PrototypeDependency
	Provider <ChatNetworkObjectHelper> chatNetworkHelper;

	// implementation

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