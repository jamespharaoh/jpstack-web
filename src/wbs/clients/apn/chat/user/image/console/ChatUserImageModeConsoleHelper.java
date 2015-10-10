package wbs.clients.apn.chat.user.image.console;

import wbs.clients.apn.chat.user.image.model.ChatUserImageMode;
import wbs.console.helper.EnumConsoleHelper;
import wbs.framework.application.annotations.SingletonComponent;

@SingletonComponent ("chatUserImageModeConsoleHelper")
public
class ChatUserImageModeConsoleHelper
	extends EnumConsoleHelper<ChatUserImageMode> {

	{

		enumClass (ChatUserImageMode.class);

		add (ChatUserImageMode.link, "link");
		add (ChatUserImageMode.mms, "mms");

	}

}
