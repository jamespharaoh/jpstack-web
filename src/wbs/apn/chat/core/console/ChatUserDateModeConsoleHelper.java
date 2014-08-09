package wbs.apn.chat.core.console;

import wbs.apn.chat.user.core.model.ChatUserDateMode;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.platform.console.helper.EnumConsoleHelper;

@SingletonComponent ("chatUserDateModeConsoleHelper")
public
class ChatUserDateModeConsoleHelper
	extends EnumConsoleHelper<ChatUserDateMode> {

	{

		enumClass (ChatUserDateMode.class);

		add (ChatUserDateMode.none, "none");
		add (ChatUserDateMode.text, "text");
		add (ChatUserDateMode.photo, "photo");

	}

}
