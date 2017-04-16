package wbs.apn.chat.user.image.console;

import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.etc.LogicUtils.referenceNotEqualWithClass;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;

import lombok.NonNull;

import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.media.console.MediaConsoleLogic;

import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUserDao;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.image.model.ChatUserImageObjectHelper;
import wbs.apn.chat.user.image.model.ChatUserImageRec;
import wbs.web.exceptions.HttpNotFoundException;

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

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MediaConsoleLogic mediaConsoleLogic;

	// state

	ChatUserRec chatUser;
	ChatUserImageRec image;

	// implementation

	@Override
	public
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		chatUser =
			chatUserHelper.findFromContextRequired ();

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

			throw new HttpNotFoundException (
				optionalAbsent (),
				emptyList ());

		}

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"renderHtmlBodyContent");

		htmlParagraphOpen ();

		mediaConsoleLogic.writeMediaContent (
			taskLogger,
			image.getMedia ());

		htmlParagraphClose ();

	}

}
