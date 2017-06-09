package wbs.apn.chat.user.admin.console;

import static wbs.utils.etc.LogicUtils.ifNotNullThenElseEmDash;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingThreeWrite;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWriteHtml;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenList;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import lombok.NonNull;

import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.html.ScriptRef;
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
import wbs.apn.chat.user.info.model.ChatUserInfoRec;

@PrototypeComponent ("chatUserAdminInfoPart")
public
class ChatUserAdminInfoPart
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

	// details

	@Override
	public
	Set<ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef> builder ()

			.addAll (
				super.scriptRefs ())

			.add (
				ConsoleApplicationScriptRef.javascript (
					"/js/gsm.js"))

			.add (
				ConsoleApplicationScriptRef.javascript (
					"/js/wbs.js"))

			.add (
				ConsoleApplicationScriptRef.javascript (
					"/js/DOM.js"))

			.build ();

	}

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

			if (
				requestContext.canContext (
					"chat.userAdmin")
			) {

				htmlFormOpenPostAction (
					formatWriter,
					requestContext.resolveLocalUrl (
						"/chatUser.admin.info"));

				htmlTableOpenDetails (
					formatWriter);

				String charCountJavascript =
					stringFormat (
						"gsmCharCount (%s, %s, %s)",
						"this",
						"document.getElementById ('chars')",
						"0");

				htmlTableDetailsRowWriteHtml (
					formatWriter,
					"Info",
					stringFormat (
						"<textarea",
						" id=\"info\"",
						" name=\"info\"",
						" rows=\"3\"",
						" cols=\"64\"",
						" onkeyup=\"%h\"",
						charCountJavascript,
						" onfocus=\"%h\"",
						charCountJavascript,
						">%h</textarea></td>",
						requestContext.formOrElse (
							"info",
							() -> ifNotNullThenElseEmDash (
								chatUser.getInfoText (),
								() -> chatUser.getInfoText ().getText ()))));

				htmlTableDetailsRowWriteHtml (
					formatWriter,
					"Chars",
					"<span id=\"chars\">&nbsp;</span>");

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
						"<input\n",
						" type=\"submit\"\n",
						" value=\"save changes\"",
						">"));

				htmlTableClose (
					formatWriter);

				htmlFormClose (
					formatWriter);

			}

			Set <ChatUserInfoRec> chatUserInfos =
				chatUser.getChatUserInfos ();

			if (chatUserInfos.size () == 0)
				return;

			htmlHeadingThreeWrite (
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
				"Moderator");

			for (
				ChatUserInfoRec chatUserInfo
					: chatUserInfos
			) {

				htmlTableRowOpen (
					formatWriter);

				htmlTableCellWrite (
					formatWriter,
					timeFormatter.timestampTimezoneString (
						chatUserLogic.getTimezone (
							chatUser),
						chatUserInfo.getCreationTime ()));

				htmlTableCellWrite (
					formatWriter,
					ifNotNullThenElseEmDash (
						chatUserInfo.getOriginalText (),
						() -> chatUserInfo.getOriginalText ().getText ()));

				htmlTableCellWrite (
					formatWriter,
					ifNotNullThenElseEmDash (
						chatUserInfo.getEditedText (),
						() -> chatUserInfo.getEditedText ().getText ()));

				htmlTableCellWrite (
					formatWriter,
					chatConsoleLogic.textForChatUserInfoStatus (
						chatUserInfo.getStatus ()));

				if (
					isNotNull (
						chatUserInfo.getModerator ())
				) {

					objectManager.writeTdForObjectMiniLink (
						transaction,
						formatWriter,
						chatUserInfo.getModerator ());

				} else {

					htmlTableCellWrite (
						formatWriter,
						"—");

				}

				htmlTableRowClose (
					formatWriter);

			}

			htmlTableClose (
				formatWriter);

		}

	}

}
