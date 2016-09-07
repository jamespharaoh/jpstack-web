package wbs.clients.apn.chat.user.admin.console;

import static wbs.framework.utils.etc.NullUtils.ifNull;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import wbs.clients.apn.chat.core.console.ChatConsoleLogic;
import wbs.clients.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.clients.apn.chat.user.core.logic.ChatUserLogic;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.info.model.ChatUserInfoRec;
import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.html.ScriptRef;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.utils.TimeFormatter;

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

	@Override
	public
	void renderHtmlBodyContent() {

		if (
			requestContext.canContext (
				"chat.userAdmin")
		) {

			printFormat (
				"<form",
				" method=\"post\"",
				" action=\"%h\"",
				requestContext.resolveLocalUrl (
					"/chatUser.admin.info"),
				">\n");

			printFormat (
				"<table",
				" class=\"details\"",
				" border=\"0\"",
				" cellspacing=\"1\"",
				">\n");

			printFormat (
				"<tr>\n",
				"<th>Info</th>\n",
				"<td><textarea",
				" id=\"info\"",
				" name=\"info\"",
				" rows=\"3\"",
				" cols=\"64\"",
				" onkeyup=\"%h\"",
				stringFormat (
					"gsmCharCount (%s, %s, %s)",
					"this",
					"document.getElementById ('chars')",
					"0"),
				" onfocus=\"%h\"",
				stringFormat (
					"gsmCharCount (%s, %s, %s)",
					"this",
					"document.getElementById ('chars')",
					"0"),
				">%h</textarea></td>\n",
				ifNull (
					requestContext.getForm ("info"),
					chatUser.getInfoText () != null
						? chatUser.getInfoText ().getText ()
						: ""),
				"</tr>\n");

			printFormat (
				"<tr>\n",
				"<th>Chars</th>\n",
				"<td><span id=\"chars\">&nbsp;</span></td>\n",
				"</tr>\n");

			printFormat (
				"<tr>\n",
				"<th>Reason</th>\n",
				"<td>%s</td>\n",
				chatConsoleLogic.selectForChatUserEditReason (
					"editReason",
					requestContext.getForm ("editReason")),
				"</tr>\n");

			printFormat (
				"<tr>\n",
				"<th>Action</th>\n",
				"<td><input\n",
				" type=\"submit\"\n",
				" value=\"save changes\"",
				"></td>\n",
				"</tr>\n");

			printFormat (
				"</table>\n");

			printFormat (
				"</form>\n");

		}

		Set<ChatUserInfoRec> chatUserInfos =
			chatUser.getChatUserInfos ();

		if (chatUserInfos.size () == 0)
			return;

		printFormat (
			"<h3>History</h3>\n");

		printFormat (
			"<table",
			" class=\"list\"",
			" border=\"0\"",
			" cellspacing=\"1\"",
			">\n");

		printFormat (
			"<tr>\n",
			"<th>Timestamp</th>\n",
			"<th>Original</th>\n",
			"<th>Edited</th>\n",
			"<th>Status</th>\n",
			"<th>Moderator</th>\n",
			"</tr>\n");

		for (
			ChatUserInfoRec chatUserInfo
				: chatUserInfos
		) {

			printFormat (
				"<tr>\n");

			printFormat (
				"<td>%h</td>\n",
				timeFormatter.timestampTimezoneString (
					chatUserLogic.getTimezone (
						chatUser),
					chatUserInfo.getCreationTime ()));

			printFormat (
				"<td>%h</td>\n",
				chatUserInfo.getOriginalText () != null
					? chatUserInfo.getOriginalText ().getText ()
					: "-");

			printFormat (
				"<td>%h</td>\n",
				chatUserInfo.getEditedText () != null
					? chatUserInfo.getEditedText ().getText ()
					: "-");

			printFormat (
				"<td>%h</td>\n",
				chatConsoleLogic.textForChatUserInfoStatus (
					chatUserInfo.getStatus ()));

			printFormat (
				chatUserInfo.getModerator () == null
					? "<td>-</td>"
					: objectManager.tdForObjectMiniLink (
						chatUserInfo.getModerator ()));

			printFormat (
				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

}
