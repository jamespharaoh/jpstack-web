package wbs.clients.apn.chat.user.image.console;

import wbs.clients.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserDao;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.image.model.ChatUserImageObjectHelper;
import wbs.clients.apn.chat.user.image.model.ChatUserImageRec;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.annotations.SingletonDependency;
import wbs.platform.media.console.MediaConsoleLogic;

@PrototypeComponent ("chatUserImageViewPart")
public
class ChatUserImageViewPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ChatUserDao chatUserDao;

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@SingletonDependency
	ChatUserImageObjectHelper chatUserImageHelper;

	@SingletonDependency
	MediaConsoleLogic mediaConsoleLogic;

	// state

	ChatUserRec chatUser;
	ChatUserImageRec image;

	// implementation

	@Override
	public
	void prepare () {

		chatUser =
			chatUserHelper.findRequired (
				requestContext.stuffInteger (
					"chatUserId"));

		image =
			chatUserImageHelper.findRequired (
				requestContext.parameterIntegerRequired (
					"chatUserImageId"));

		if (image.getChatUser () != chatUser)
			image = null;

	}

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<p>%s</p>\n",
			mediaConsoleLogic.mediaContent (
				image.getMedia ()));

	}

}
