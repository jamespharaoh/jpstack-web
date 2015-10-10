package wbs.clients.apn.chat.tv.moderation.console;

import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.SingletonComponent;

@SingletonComponent ("chatTvModerationSummaryPart")
public
class ChatTvModerationSummaryPart
	extends AbstractPagePart {

	/*

	@Inject
	ChatConsoleLogicImpl chatConsoleLogic;

	@Inject
	ChatCreditLogic chatCreditLogic;

	@Inject
	ChatTvDao chatTvDao;

	@Inject
	ConsoleObjectManager consoleObjectManager;

	@Inject
	MediaConsoleLogic mediaConsoleLogic;

	ChatTvModerationRec moderation;
	ChatTvMessageRec message;
	ChatUserRec chatUser;

	List<ChatTvMessageRec> allMessages;
	List<ChatTvMessageRec> userMessages;

	@Override
	public
	void prepare () {

		moderation = chatTvDao.findModerationById (
			requestContext.stuffInt ("chatTvModerationId"));

		message = moderation.getMessage ();

		chatUser = moderation.getChatUser ();

		allMessages = chatTvDao.findMessagesRecent (
			requestContext.stuffInt ("chatId"),
			ChatTvModerationConsoleLogic.RECENT_GLOBAL_MESSAGES);

		userMessages = chatTvDao.findMessagesRecentByUser (
			requestContext.stuffInt ("chatId"),
			chatUser.getId (),
			ChatTvModerationConsoleLogic.RECENT_USER_MESSAGES);
	}

	@Override
	public
	void goBodyStuff () {

		pf ("<table class=\"details\" border=\"0\" cellspacing=\"1\">\n");

		pf ("<tr> <th>User</th> <td>%h</td> </tr>\n",
			chatUser.getCode ());

		pf ("<tr> <th>Message</th> <td>%h</td> </tr>\n",
			message.getOriginalText ());

		if (message.getMedia () != null)
			pf ("<tr> <th>Image</th> <td>%s</td> </tr>\n",
				mediaConsoleLogic.mediaContent (
					message.getMedia ()));

		pf ("<tr> <th>Date mode</th> <td>%h</td> </tr>\n",
			chatUser.getDateMode ());

		pf ("<tr> <th>Online</th> <td>%h</td> </tr>\n",
			chatUser.getOnline () ? "yes" : "no");

		pf ("<tr> <th>Gender</th> <td>%h</td> </tr>\n",
			chatUser.getGender ());

		pf ("<tr> <th>Orientation</th> <td>%h</td> </tr>\n",
			chatUser.getOrient ());

		pf ("<tr> <th>Name</th> <td>%h</td> </tr>\n",
			chatUser.getName ());

		pf ("<tr> <th>Info</th> <td>%h</td> </tr>\n",
			chatUser.getInfoText ());

		pf ("<tr> <th>Credit</th> <td>%s</td> </tr>\n",
			chatConsoleLogic.creditHtml (chatUser.getCredit ()));

		pf ("<tr> <th>Barred</th> <td>%h</td> </tr>\n",
			chatUser.getBarred () ? "yes" : "no");

		pf ("<tr> <th>Credit mode</th> <td>%h</td> </tr>\n",
			chatUser.getCreditMode ());

		if (chatUser.getCreditMode () == ChatUserCreditMode.strict) {

			pf ("<tr>\n",

				"<th>Temp barred</th>\n",

				"<td>%h</td>\n",
				chatCreditLogic.userStrictCreditOk (chatUser) ? "no" : "yes",

				"</tr>\n");

		}

		pf ("</table>");

		pf ("<h2>User's recent messages</h2>\n");

		pf ("<table class=\"list\">\n");

		pf ("<tr>\n",
			"<th colspan=\"2\">Message</th>\n",
			"<th>Timestamp</th>\n",
			"<th>Status</th>\n",
			"<th>User</th>\n",
			"</tr>\n");

		for (ChatTvMessageRec message : userMessages) {

			ChatUserRec chatUser = message.getChatUser ();
			ChatRec chat = chatUser.getChat ();
			UserRec user = message.getUser ();

			pf ("<tr>\n",

				"<td>%s</td>\n",
				message.getMedia () != null
					? mediaConsoleLogic.mediaThumb32 (
						message.getMedia ())
					: "",

				"<td>%h</td>\n",
				message.getOriginalText ().toString (),

				"<td>%h</td>\n",
				Misc.timestampFormatSeconds.format (
					message.getCreatedTime ()),

				"<td>%h</td>\n",
				message.getStatus ().toString (),

				"%s\n",
				consoleObjectManager.tdForObject (
					user,
					chat.getSlice (),
					true,
					true),

				"</tr>\n");
		}

		pf ("</table>\n");

		pf ("<h2>All recent messages</h2>\n");

		pf ("<table class=\"list\">\n");

		pf ("<tr>\n",
			"<th>Chat user</th>\n",
			"<th colspan=\"2\">Message</th>\n",
			"<th>Timestamp</th>\n",
			"<th>Status</th>\n",
			"<th>User</th>\n",
			"</tr>\n");

		for (ChatTvMessageRec message : allMessages) {

			ChatUserRec chatUser = message.getChatUser ();
			ChatRec chat = chatUser.getChat ();
			UserRec user = message.getUser ();

			pf ("<tr>\n",

				"%s\n",
				consoleObjectManager.tdForObject (
					chatUser,
					chat,
					true,
					true),

				"<td>%s</td>\n",
				message.getMedia () != null
					? mediaConsoleLogic.mediaThumb32 (
						message.getMedia ())
					: "",

				"<td>%h</td>\n",
				message.getEditedText ().toString (),

				"<td>%h</td>\n",
				Misc.timestampFormatSeconds.format (
					message.getModeratedTime ()),

				"<td>%h</td>\n",
				message.getStatus ().toString (),

				"%s\n",
				consoleObjectManager.tdForObject (
					user,
					chat.getSlice (),
					true,
					true),

				"</tr>\n");

		}

		pf ("</table>\n");

	}
	*/

}