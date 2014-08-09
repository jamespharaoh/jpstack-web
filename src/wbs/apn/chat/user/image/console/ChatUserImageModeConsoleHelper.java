package wbs.apn.chat.user.image.console;

import wbs.apn.chat.user.image.model.ChatUserImageMode;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.platform.console.helper.EnumConsoleHelper;

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
