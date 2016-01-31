package wbs.clients.apn.chat.user.core.console;

import wbs.clients.apn.chat.user.core.model.ChatUserType;
import wbs.console.helper.EnumConsoleHelper;
import wbs.framework.application.annotations.SingletonComponent;

@SingletonComponent ("chatUserTypeConsoleHelper")
public
class ChatUserTypeConsoleHelper
	extends EnumConsoleHelper<ChatUserType> {

	{

		enumClass (ChatUserType.class);

		auto ();

	}

}
