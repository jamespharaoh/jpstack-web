package wbs.apn.chat.user.image.console;

import static wbs.utils.etc.LogicUtils.referenceNotEqualWithClass;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphClose;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphOpen;

import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUserDao;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.image.model.ChatUserImageObjectHelper;
import wbs.apn.chat.user.image.model.ChatUserImageRec;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.web.PageNotFoundException;
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

		if (
			referenceNotEqualWithClass (
				ChatUserRec.class,
				image.getChatUser (),
				chatUser)
		) {
			throw new PageNotFoundException ();
		}

	}

	@Override
	public
	void renderHtmlBodyContent () {

		htmlParagraphOpen ();

		mediaConsoleLogic.writeMediaContent (
			image.getMedia ());

		htmlParagraphClose ();

	}

}
