package wbs.apn.chat.bill.logic;

import com.google.common.base.Optional;

import wbs.apn.chat.bill.model.ChatNetworkObjectHelperMethods;
import wbs.apn.chat.bill.model.ChatNetworkObjectHelper;
import wbs.apn.chat.bill.model.ChatNetworkRec;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.component.annotations.WeakSingletonDependency;

public
class ChatNetworkObjectHelperMethodsImplementation
	implements ChatNetworkObjectHelperMethods {

	// singleton dependencies

	@WeakSingletonDependency
	ChatNetworkObjectHelper chatNetworkHelper;

	// implementation

	@Override
	public
	Optional <ChatNetworkRec> forUser (
			ChatUserRec chatUser) {

		ChatNetworkRec chatNetwork =
			chatNetworkHelper.find (
				chatUser.getChat (),
				chatUser.getNumber ().getNetwork ());

		return Optional.fromNullable (
			chatNetwork);

	}

}