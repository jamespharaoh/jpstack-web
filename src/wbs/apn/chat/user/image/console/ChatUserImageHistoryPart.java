package wbs.apn.chat.user.image.console;

import static wbs.utils.etc.EnumUtils.enumNotEqualSafe;
import static wbs.utils.etc.LogicUtils.ifNotNullThenElseEmDash;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.toEnum;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.web.utils.HtmlAttributeUtils.htmlAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlColumnSpanAttribute;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenList;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import lombok.NonNull;

import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.media.console.MediaConsoleLogic;
import wbs.platform.user.console.UserConsoleLogic;

import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.image.model.ChatUserImageRec;
import wbs.apn.chat.user.image.model.ChatUserImageType;

@PrototypeComponent ("chatUserImageHistoryPart")
public
class ChatUserImageHistoryPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MediaConsoleLogic mediaConsoleLogic;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// state

	ChatUserImageType type;
	ChatUserRec chatUser;
	Set <ChatUserImageRec> chatUserImages;

	// implementation

	@Override
	public
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		type =
			toEnum (
				ChatUserImageType.class,
				requestContext.stuffString (
					"chatUserImageType"));

		chatUser =
			chatUserHelper.findFromContextRequired ();

		chatUserImages =
			new TreeSet<> (
				chatUser.getChatUserImages ());

		Iterator <ChatUserImageRec> iterator =
			chatUserImages.iterator ();

		while (iterator.hasNext ()) {

			if (
				enumNotEqualSafe (
					iterator.next ().getType (),
					type)
			) {
				iterator.remove ();
			}

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

		htmlTableOpenList ();

		htmlTableHeaderRowWrite (
			"Type",
			"Index",
			"Preview",
			"Timestamp",
			"Status",
			"Moderator",
			"Moderated");

		if (chatUserImages.isEmpty ()) {

			htmlTableRowOpen ();

			htmlTableCellWrite (
				"No history to show",
				htmlColumnSpanAttribute (7l));

			htmlTableRowClose ();

		}

		for (
			ChatUserImageRec chatUserImage
				: chatUserImages
		) {

			htmlTableRowOpen ();

			// type

			htmlTableCellWrite (
				chatUserImage.getType ().name ());

			// index

			htmlTableCellWrite (
				ifNotNullThenElseEmDash (
					chatUserImage.getIndex (),
					() -> integerToDecimalString (
						chatUserImage.getIndex () + 1)));

			// media

			htmlTableCellOpen (
				htmlAttribute (
					"style",
					"text-align: center"));

			if (
				isNotNull (
					chatUserImage.getMedia ())
			) {

				mediaConsoleLogic.writeMediaThumb100 (
					taskLogger,
					chatUserImage.getMedia ());

			} else {

				formatWriter.writeLineFormat (
					"(none)");

			}

			htmlTableCellClose ();

			// timestamp

			htmlTableCellWrite (
				userConsoleLogic.timestampWithoutTimezoneString (
					chatUserImage.getTimestamp ()));

			// status

			htmlTableCellWrite (
				chatUserImage.getStatus ().name ());

			// moderator

			htmlTableCellWrite (
				objectManager.objectPathMini (
					chatUserImage.getModerator (),
					userConsoleLogic.sliceRequired ()));

			// moderation time

			htmlTableCellWrite (
				userConsoleLogic.timestampWithoutTimezoneString (
					chatUserImage.getModerationTime ()));

			// end row

			htmlTableRowClose ();

		}

		htmlTableClose ();

	}

}
