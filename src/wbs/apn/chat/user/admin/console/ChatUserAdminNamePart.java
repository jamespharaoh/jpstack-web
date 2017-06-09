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
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.string.FormatWriter;
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
	ConsoleRequestContext requestContext;

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
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContent");

		) {

			htmlFormOpenPostAction (
				formatWriter,
				requestContext.resolveLocalUrl (
					"/chatUser.admin.name"));

			htmlTableOpenDetails (
				formatWriter);

			htmlTableDetailsRowWriteHtml (
				formatWriter,
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
				formatWriter,
				"Reason",
				() -> chatConsoleLogic.writeSelectForChatUserEditReason (
					formatWriter,
					"editReason",
					requestContext.formOrEmptyString (
						"editReason")));

			htmlTableDetailsRowWriteHtml (
				formatWriter,
				"Action",
				stringFormat (
					"<input",
					" type=\"submit\"",
					" value=\"update name\"",
					">"));

			htmlTableClose (
				formatWriter);

			htmlFormClose (
				formatWriter);

			htmlHeadingTwoWrite (
				formatWriter,
				"History");

			htmlTableOpenList (
				formatWriter);

			htmlTableHeaderRowWrite (
				formatWriter,
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

				htmlTableRowOpen (
					formatWriter);

				htmlTableCellWrite (
					formatWriter,
					timeFormatter.timestampTimezoneString (
						chatUserLogic.getTimezone (
							chatUser),
						chatUserName.getCreationTime ()));

				htmlTableCellWrite (
					formatWriter,
					emptyStringIfNull (
						chatUserName.getOriginalName ()));

				htmlTableCellWrite (
					formatWriter,
					emptyStringIfNull (
						chatUserName.getEditedName ()));

				htmlTableCellWrite (
					formatWriter,
					chatConsoleLogic.textForChatUserInfoStatus (
						chatUserName.getStatus ()));

				htmlTableCellWrite (
					formatWriter,
					chatConsoleLogic.textForChatUserEditReason (
						chatUserName.getEditReason ()));

				objectManager.writeTdForObjectMiniLink (
					transaction,
					formatWriter,
					chatUserName.getModerator ());

				htmlTableCellClose (
					formatWriter);

			}

			htmlTableClose (
				formatWriter);

		}

	}

}
