package wbs.apn.chat.user.image.console;

import static wbs.utils.etc.EnumUtils.enumNotEqualSafe;
import static wbs.utils.etc.LogicUtils.ifNotNullThenElseEmDash;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.toEnum;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.web.HtmlAttributeUtils.htmlAttribute;
import static wbs.utils.web.HtmlAttributeUtils.htmlColumnSpanAttribute;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellOpen;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenList;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowOpen;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.image.model.ChatUserImageRec;
import wbs.apn.chat.user.image.model.ChatUserImageType;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.platform.media.console.MediaConsoleLogic;
import wbs.platform.user.console.UserConsoleLogic;

@PrototypeComponent ("chatUserImageHistoryPart")
public
class ChatUserImageHistoryPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

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
	void prepare () {

		type =
			toEnum (
				ChatUserImageType.class,
				requestContext.stuffString (
					"chatUserImageType"));

		chatUser =
			chatUserHelper.findRequired (
				requestContext.stuffInteger (
					"chatUserId"));

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
	void renderHtmlBodyContent () {

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
