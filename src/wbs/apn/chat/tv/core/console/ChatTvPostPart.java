package wbs.apn.chat.tv.core.console;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.part.AbstractPagePart;

@PrototypeComponent ("chatTvPostPart")
public
class ChatTvPostPart
	extends AbstractPagePart {

	/*

	@Inject
	ChatTvDao chatTvDao;

	@Inject
	ConsoleObjectManager consoleObjectManager;

	@Inject
	MediaConsoleLogic mediaConsoleLogic;

	List<ChatTvMessageRec> messages;

	@Override
	public
	void prepare () {

		messages =
			chatTvDao.findMessagesRecent (
				requestContext.stuffInt ("chatId"),
				ChatTvConsoleLogic.RECENT_MESSAGE_COUNT);

	}

	@Override
	public
	void goBodyStuff () {

		@SuppressWarnings ("unchecked")
		Map<String,String> params = (Map<String,String>)
			requestContext.session ("chatTvPostParams");

		if (params == null)
			params = new HashMap<String,String> ();

		pf ("<form method=\"post\" action=\"%h\">\n",
			requestContext.localUrl ("/chat_tv_post"));

		pf ("<table class=\"details\">\n");

		pf ("<tr> <th>From user code</th> <td>%s</td> </tr>\n",
			sf ("<input type=\"text\" name=\"fromUserCode\" value=\"%h\">",
				Misc.toString (params.get ("fromUserCode"))));

		pf ("<tr> <th>Message</th> <td>%s</td> </tr>\n",
			sf ("<input type=\"text\" name=\"message\" value=\"%h\">",
				Misc.toString (params.get ("message"))));

		pf ("</table>\n");

		pf ("<p><input type=\"submit\" name=\"postAsUser\" " +
				"value=\"post as user\">\n",

			"<input type=\"submit\" name=\"postAsTextJockey\" " +
				"value=\"post as text jockey\"></p>\n");

		pf ("</form>\n");

		pf ("<table class=\"list\">\n");

		pf ("<tr> ",
			"<th>Chat user</th> ",
			"<th>User</th> ",
			"<th colspan=\"2\">Message</th> ",
			"<th>Timestamp</th> ",
			"<th>Status</th> ",
			"</tr>\n");

		for (ChatTvMessageRec message : messages) {

			ChatUserRec chatUser = message.getChatUser ();
			ChatRec chat = chatUser.getChat ();
			UserRec user = message.getUser ();

			pf ("<tr> ",

				"%s ",
				consoleObjectManager.tdForObject (
					chatUser,
					chat,
					true,
					true),

				"%s ",
				consoleObjectManager.tdForObject (
					user,
					null,
					true,
					true),

				"<td>%s</td> ",
				message.getMedia () != null ?
					mediaConsoleLogic.mediaThumb32 (
						message.getMedia ())
					: "",

				"<td>%h</td> ",
				message.getEditedText ().toString (),

				"<td>%h</td> ",
				Misc.timestampFormatSeconds.format (
					message.getModeratedTime ()),

				"<td>%h</td> ",
				message.getStatus ().toString (),

				"</tr>\n");

		}

		pf ("</table>\n");
	}
	*/

}