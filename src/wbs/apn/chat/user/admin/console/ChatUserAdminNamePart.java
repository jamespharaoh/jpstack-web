package wbs.apn.chat.user.admin.console;

import static wbs.utils.etc.LogicUtils.ifNullThenEmDash;
import static wbs.utils.string.StringUtils.emptyStringIfNull;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWriteHtml;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenList;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;

import lombok.NonNull;

import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.time.TimeFormatter;

import wbs.apn.chat.core.console.ChatConsoleLogic;
import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.info.model.ChatUserNameRec;

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

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	TimeFormatter timeFormatter;

	// state

	ChatUserRec chatUser;

	// implementation

	@Override
	public
	void prepare (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepare");

		) {

			chatUser =
				chatUserHelper.findFromContextRequired (
					transaction);

		}

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContent");

		) {

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
					requestContext.formOrElse (
						"name",
						() -> ifNullThenEmDash (
							chatUser.getName ())),
					">"));

			htmlTableDetailsRowWriteHtml (
				"Reason",
				() -> chatConsoleLogic.writeSelectForChatUserEditReason (
					"editReason",
					requestContext.formOrEmptyString (
						"editReason")));

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
					transaction,
					chatUserName.getModerator ());

				htmlTableCellClose ();

			}

			htmlTableClose ();

		}

	}

}
