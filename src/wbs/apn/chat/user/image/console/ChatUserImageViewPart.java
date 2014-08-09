package wbs.apn.chat.user.image.console;

import javax.inject.Inject;

import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUserDao;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.image.model.ChatUserImageObjectHelper;
import wbs.apn.chat.user.image.model.ChatUserImageRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.part.AbstractPagePart;
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
			chatUserHelper.find (
				requestContext.stuffInt ("chatUserId"));

		image =
			chatUserImageHelper.find (
				requestContext.parameterInt ("chatUserImageId"));

		if (image.getChatUser () != chatUser)
			image = null;

	}

	@Override
	public
	void goBodyStuff () {

		printFormat (
			"<p>%s</p>\n",
			mediaConsoleLogic.mediaContent (
				image.getMedia ()));

	}

}
