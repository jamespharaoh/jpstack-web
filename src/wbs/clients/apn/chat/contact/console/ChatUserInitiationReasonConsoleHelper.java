package wbs.clients.apn.chat.contact.console;

import wbs.clients.apn.chat.contact.model.ChatUserInitiationReason;
import wbs.console.helper.EnumConsoleHelper;
import wbs.framework.application.annotations.SingletonComponent;

@SingletonComponent ("chatUserInitiationReasonConsoleHelper")
public
class ChatUserInitiationReasonConsoleHelper
	extends EnumConsoleHelper<ChatUserInitiationReason> {

	{

		enumClass (ChatUserInitiationReason.class);

		auto ();

	}

}
