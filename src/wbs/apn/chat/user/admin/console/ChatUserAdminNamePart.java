package wbs.apn.chat.user.admin.console;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.StringUtils.emptyStringIfNull;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.web.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.utils.web.HtmlFormUtils.htmlFormClose;
import static wbs.utils.web.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableDetailsRowWriteHtml;
import static wbs.utils.web.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenDetails;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenList;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowOpen;

import wbs.apn.chat.core.console.ChatConsoleLogic;
import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.info.model.ChatUserNameRec;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.utils.time.TimeFormatter;

@PrototypeComponent ("chatUserAdminNamePart")
public
class ChatUserAdminNamePart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ChatConsoleLogic chatConsoleLogic;

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@SingletonDependency
	ChatUserLogic chatUserLogic;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	TimeFormatter timeFormatter;

	// state

	ChatUserRec chatUser;

	// implementation

	@Override
	public
	void prepare () {

		chatUser =
			chatUserHelper.findRequired (
				requestContext.stuffInteger (
					"chatUserId"));

	}

	@Override
	public
	void renderHtmlBodyContent () {

		htmlFormOpenPostAction (
			requestContext.resolveLocalUrl (
				"/chatUser.admin.name"));

		htmlTableOpenDetails ();

		htmlTableDetailsRowWriteHtml (
			"Name",
			stringFormat (
				"<input",
				" type=\"text\"",
				" name=\"name\"",
				" value=\"%h\"",
				ifNull (
					requestContext.getForm ("name"),
					chatUser.getName (),
					""),
				">"));

		htmlTableDetailsRowWriteHtml (
			"Reason",
			() -> chatConsoleLogic.writeSelectForChatUserEditReason (
				"editReason",
				emptyStringIfNull (
					requestContext.getForm (
						"editReason"))));

		htmlTableDetailsRowWriteHtml (
			"Action",
			stringFormat (
				"<input",
				" type=\"submit\"",
				" value=\"update name\"",
				">"));

		htmlTableClose ();

		htmlFormClose ();

		htmlHeadingTwoWrite (
			"History");

		htmlTableOpenList ();

		htmlTableHeaderRowWrite (
			"Timestamp",
			"Original",
			"Edited",
			"Status",
			"Reason",
			"Moderator");

		for (
			ChatUserNameRec chatUserName
				: chatUser.getChatUserNames ()
		) {

			htmlTableRowOpen ();

			htmlTableCellWrite (
				timeFormatter.timestampTimezoneString (
					chatUserLogic.getTimezone (
						chatUser),
					chatUserName.getCreationTime ()));

			htmlTableCellWrite (
				emptyStringIfNull (
					chatUserName.getOriginalName ()));

			htmlTableCellWrite (
				emptyStringIfNull (
					chatUserName.getEditedName ()));

			htmlTableCellWrite (
				chatConsoleLogic.textForChatUserInfoStatus (
					chatUserName.getStatus ()));

			htmlTableCellWrite (
				chatConsoleLogic.textForChatUserEditReason (
					chatUserName.getEditReason ()));

			objectManager.writeTdForObjectMiniLink (
				chatUserName.getModerator ());

			htmlTableCellClose ();

		}

		htmlTableClose ();

	}

}
