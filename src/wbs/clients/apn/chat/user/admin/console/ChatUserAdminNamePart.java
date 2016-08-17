package wbs.clients.apn.chat.user.admin.console;

import static wbs.framework.utils.etc.Misc.ifNull;

import javax.inject.Inject;

import wbs.clients.apn.chat.core.console.ChatConsoleLogic;
import wbs.clients.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.clients.apn.chat.user.core.logic.ChatUserLogic;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.info.model.ChatUserNameRec;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.TimeFormatter;

@PrototypeComponent ("chatUserAdminNamePart")
public
class ChatUserAdminNamePart
	extends AbstractPagePart {

	// dependencies

	@Inject
	ChatConsoleLogic chatConsoleLogic;

	@Inject
	ChatUserConsoleHelper chatUserHelper;

	@Inject
	ChatUserLogic chatUserLogic;

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
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

		printFormat (
			"<form",
			" method=\"post\"",
			" action=\"%h\"",
			requestContext.resolveLocalUrl (
				"/chatUser.admin.name"),
			">\n");

		printFormat (
			"<table class=\"details\">\n");

		printFormat (
			"<tr>\n",
			"<th>Name</th>\n",

			"<td><input",
			" type=\"text\"",
			" name=\"name\"",
			" value=\"%h\"",
			ifNull (
				requestContext.getForm ("name"),
				chatUser.getName (),
				""),
			"></td>\n",

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

			"<td><input",
			" type=\"submit\"",
			" value=\"update name\"",
			"></td>\n",

			"</tr>\n");

		printFormat (
			"</table>\n");

		printFormat (
			"</form>\n");

		printFormat (
			"<h2>History</h2>\n");

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th>Timestamp</th>\n",
			"<th>Original</th>\n",
			"<th>Edited</th>\n",
			"<th>Status</th>\n",
			"<th>Reason</th>\n",
			"<th>Moderator</th>\n",
			"</tr>\n");

		for (ChatUserNameRec chatUserName
				: chatUser.getChatUserNames ()) {

			printFormat (
				"<tr>\n",

				"<td>%h</td>\n",
				timeFormatter.timestampTimezoneString (
					chatUserLogic.timezone (
						chatUser),
					chatUserName.getCreationTime ()),

				"<td>%h</td>\n",
				chatUserName.getOriginalName (),

				"<td>%h</td>\n",
				chatUserName.getEditedName (),

				"<td>%h</td>\n",
				chatConsoleLogic.textForChatUserInfoStatus (
					chatUserName.getStatus ()),

				"<td>%h</td>\n",
				chatConsoleLogic.textForChatUserEditReason (
					chatUserName.getEditReason ()),

				"%s\n",
				objectManager.tdForObjectMiniLink (
					chatUserName.getModerator ()),

				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

}
