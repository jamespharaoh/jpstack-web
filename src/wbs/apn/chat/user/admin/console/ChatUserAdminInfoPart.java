package wbs.apn.chat.user.admin.console;

import static wbs.utils.etc.LogicUtils.ifNotNullThenElseEmDash;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.string.StringUtils.emptyStringIfNull;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.web.HtmlBlockUtils.htmlHeadingThreeWrite;
import static wbs.utils.web.HtmlFormUtils.htmlFormClose;
import static wbs.utils.web.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableDetailsRowWriteHtml;
import static wbs.utils.web.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenDetails;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenList;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowOpen;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import wbs.apn.chat.core.console.ChatConsoleLogic;
import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.info.model.ChatUserInfoRec;
import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.html.ScriptRef;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.utils.time.TimeFormatter;

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

	@SingletonDependency
	ConsoleObjectManager objectManager;

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
	void prepare () {

		chatUser =
			chatUserHelper.findRequired (
				requestContext.stuffInteger (
					"chatUserId"));

	}

	@SuppressWarnings ("deprecation")
	@Override
	public
	void renderHtmlBodyContent() {

		if (
			requestContext.canContext (
				"chat.userAdmin")
		) {

			htmlFormOpenPostAction (
				requestContext.resolveLocalUrl (
					"/chatUser.admin.info"));

			htmlTableOpenDetails ();

			String charCountJavascript =
				stringFormat (
					"gsmCharCount (%s, %s, %s)",
					"this",
					"document.getElementById ('chars')",
					"0");

			htmlTableDetailsRowWriteHtml (
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
				"Chars",
				"<span id=\"chars\">&nbsp;</span>");

			htmlTableDetailsRowWriteHtml (
				"Reason",
				() -> chatConsoleLogic.writeSelectForChatUserEditReason (
					"editReason",
					requestContext.formOrEmptyString (
						"editReason")));

			htmlTableDetailsRowWriteHtml (
				"Action",
				stringFormat (
					"<input\n",
					" type=\"submit\"\n",
					" value=\"save changes\"",
					">"));

			htmlTableClose ();

			htmlFormClose ();

		}

		Set <ChatUserInfoRec> chatUserInfos =
			chatUser.getChatUserInfos ();

		if (chatUserInfos.size () == 0)
			return;

		htmlHeadingThreeWrite (
			"History");

		htmlTableOpenList ();

		htmlTableHeaderRowWrite (
			"Timestamp",
			"Original",
			"Edited",
			"Status",
			"Moderator");

		for (
			ChatUserInfoRec chatUserInfo
				: chatUserInfos
		) {

			htmlTableRowOpen ();

			htmlTableCellWrite (
				timeFormatter.timestampTimezoneString (
					chatUserLogic.getTimezone (
						chatUser),
					chatUserInfo.getCreationTime ()));

			htmlTableCellWrite (
				ifNotNullThenElseEmDash (
					chatUserInfo.getOriginalText (),
					() -> chatUserInfo.getOriginalText ().getText ()));

			htmlTableCellWrite (
				ifNotNullThenElseEmDash (
					chatUserInfo.getEditedText (),
					() -> chatUserInfo.getEditedText ().getText ()));

			htmlTableCellWrite (
				chatConsoleLogic.textForChatUserInfoStatus (
					chatUserInfo.getStatus ()));

			if (
				isNotNull (
					chatUserInfo.getModerator ())
			) {

				objectManager.writeTdForObjectMiniLink (
					chatUserInfo.getModerator ());

			} else {

				htmlTableCellWrite (
					"—");

			}

			htmlTableRowClose ();

		}

		htmlTableClose ();

	}

}
