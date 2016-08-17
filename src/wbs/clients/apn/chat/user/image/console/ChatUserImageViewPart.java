package wbs.clients.apn.chat.user.image.console;

import javax.inject.Inject;

import wbs.clients.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserDao;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.image.model.ChatUserImageObjectHelper;
import wbs.clients.apn.chat.user.image.model.ChatUserImageRec;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.media.console.MediaConsoleLogic;

@PrototypeComponent ("chatUserImageViewPart")
public
class ChatUserImageViewPart
	extends AbstractPagePart {

	@Inject
	ChatUserDao chatUserDao;

	@Inject
	ChatUserConsoleHelper chatUserHelper;

	@Inject
	ChatUserImageObjectHelper chatUserImageHelper;

	@Inject
	MediaConsoleLogic mediaConsoleLogic;

	ChatUserRec chatUser;
	ChatUserImageRec image;

	@Override
	public
	void prepare () {

		chatUser =
			chatUserHelper.findRequired (
				requestContext.stuffInteger (
					"chatUserId"));

		image =
			chatUserImageHelper.findRequired (
				requestContext.parameterInteger (
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
