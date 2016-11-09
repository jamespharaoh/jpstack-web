package wbs.apn.chat.user.image.console;

import static wbs.utils.etc.EnumUtils.enumName;
import static wbs.utils.etc.EnumUtils.enumNameSpaces;
import static wbs.utils.etc.LogicUtils.ifNotNullThenElse;
import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.Misc.toEnum;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalValueEqualWithClass;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.web.HtmlAttributeUtils.htmlAttribute;
import static wbs.utils.web.HtmlAttributeUtils.htmlColumnSpanAttribute;
import static wbs.utils.web.HtmlFormUtils.htmlFormClose;
import static wbs.utils.web.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellOpen;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenList;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowOpen;
import static wbs.utils.web.HtmlUtils.htmlLinkWriteHtml;

import java.util.List;

import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.image.model.ChatUserImageRec;
import wbs.apn.chat.user.image.model.ChatUserImageType;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.platform.media.console.MediaConsoleLogic;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserObjectHelper;

@PrototypeComponent ("chatUserImageListPart")
public
class ChatUserImageListPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@SingletonDependency
	ChatUserLogic chatUserLogic;

	@SingletonDependency
	MediaConsoleLogic mediaConsoleLogic;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserObjectHelper userHelper;

	// state

	ChatUserImageType type;
	ChatUserRec chatUser;

	List <ChatUserImageRec> chatUserImages;

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

		switch (type) {

		case image:

			chatUserImages =
				chatUser.getChatUserImageList ();

			break;

		case video:

			chatUserImages =
				chatUser.getChatUserVideoList ();

			break;

		default:

			throw new RuntimeException (
				stringFormat (
					"Unknown chat user image type: %s",
					enumNameSpaces (
						type)));

		}

	}

	@Override
	public
	void renderHtmlBodyContent () {

		htmlFormOpenPostAction (
			requestContext.resolveLocalUrl (
				stringFormat (
					"/chatUser.%s.list",
					type.name ())));

		htmlTableOpenList ();

		htmlTableHeaderRowWrite (
			"I",
			"S",
			"Preview",
			"Timestamp",
			"Moderator",
			"Classification",
			"Controls");

		if (chatUserImages.isEmpty ()) {

			htmlTableRowOpen ();

			htmlTableCellWrite (
				"No photos/videos to show",
				htmlColumnSpanAttribute (7l));

			htmlTableRowClose ();

		}

		int index = 0;

		for (
			ChatUserImageRec chatUserImage
				: chatUserImages
		) {

			htmlTableRowOpen ();

			htmlTableCellWrite (
				ifNotNullThenElse (
					chatUserImage.getIndex (),

				() -> integerToDecimalString (
					chatUserImage.getIndex () + 1),

				() -> "—"

			));

			htmlTableCellWrite (
				ifThenElse (
					optionalValueEqualWithClass (
						ChatUserImageRec.class,
						optionalFromNullable (
							chatUserLogic.getMainChatUserImageByType (
								chatUser,
								type)),
						chatUserImage),
					() -> "Y",
					() -> ""));

			htmlTableCellOpen (
				htmlAttribute (
					"style",
					"text-align: center"));

			htmlLinkWriteHtml (
				requestContext.resolveLocalUrl (
					stringFormat (
						"/chatUser.%u.view",
						enumName (
							type),
						"?chatUserImageId=%u",
						integerToDecimalString (
							chatUserImage.getId ()))),
				() -> ifNotNullThenElse (
					chatUserImage.getMedia (),
					() -> mediaConsoleLogic.writeMediaThumb100 (
						chatUserImage.getMedia ()),
					() -> formatWriter.writeLineFormat (
						"(none)")));

			htmlTableCellClose ();

			htmlTableCellWrite (
				userConsoleLogic.timestampWithoutTimezoneString (
					chatUserImage.getTimestamp ()));

			htmlTableCellWrite (
				objectManager.objectPathMini (
					chatUserImage.getModerator (),
					userConsoleLogic.sliceRequired ()));

			htmlTableCellWrite (
				chatUserImage.getClassification ());

			htmlTableCellOpen ();

			formatWriter.writeLineFormat (
				"<input",
				" type=\"submit\"",
				" name=\"remove_%h\"",
				integerToDecimalString (
					index),
				" value=\"X\"",
				">");

			formatWriter.writeLineFormat (
				"<input",
				" type=\"submit\"",
				" name=\"rotate_ccw_%h\"",
				integerToDecimalString (
					index),
				" value=\"&#x21b6;\"",
				">");

			formatWriter.writeLineFormat (
				"<input",
				" type=\"submit\"",
				" name=\"rotate_cw_%h\"",
				integerToDecimalString (
					index),
				" value=\"&#x21b7;\"",
				">");

			formatWriter.writeLineFormat (
				"<input",
				" type=\"submit\"",
				" name=\"move_up_%h\"",
				integerToDecimalString (
					index),
				" value=\"&#x2191;\"",
				">");

			formatWriter.writeLineFormat (
				"<input",
				" type=\"submit\"",
				" name=\"move_down_%h\"",
				integerToDecimalString (
					index),
				" value=\"&#x2193;\"",
				">");

			formatWriter.writeLineFormat (
				"<input",
				" type=\"submit\"",
				" name=\"select_%h\"",
				integerToDecimalString (
					index),
				" value=\"S\"",
				">");

			htmlTableCellClose ();

			htmlTableRowClose ();

			index ++;

		}

		htmlTableClose ();

		htmlFormClose ();

	}

}
