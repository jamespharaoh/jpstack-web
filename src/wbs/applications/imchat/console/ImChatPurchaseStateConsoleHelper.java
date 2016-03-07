package wbs.applications.imchat.console;

import wbs.applications.imchat.model.ImChatPurchaseState;
import wbs.console.helper.EnumConsoleHelper;
import wbs.framework.application.annotations.SingletonComponent;

@SingletonComponent ("imChatPurchaseStateConsoleHelper")
public
class ImChatPurchaseStateConsoleHelper
	extends EnumConsoleHelper<ImChatPurchaseState> {

	{

		enumClass (
			ImChatPurchaseState.class);

		auto ();

	}

}
