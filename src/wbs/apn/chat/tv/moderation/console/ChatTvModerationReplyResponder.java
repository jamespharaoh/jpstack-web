package wbs.apn.chat.tv.moderation.console;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.responder.HtmlResponder;

@PrototypeComponent ("chatTvModerationReplyResponder")
public
class ChatTvModerationReplyResponder
	extends HtmlResponder {

	/*
	@Inject
	ChatTvDao chatTvDao;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	ConsoleManager consoleManager;

	String postUrl;

	@Override
	public
	void prepare () {

		ChatTvModerationRec moderation =
			chatTvDao.findModerationById (
				requestContext.stuffInt ("chatTvModerationId"));

		postUrl =
			moderation != null
				? requestContext.localUrl ("/chatTvModeration_reply")
				: sf (
					"%s/%d/chatTvModeration_reply",
					consoleManager
						.context ("chat_tv")
						.pathPrefix (),
					requestContext.stuffInt ("chatId"));

	}

	@Override
	public
	void goBodyStuff () {

		@SuppressWarnings ("unchecked")
		Map<String,String> params = (Map<String,String>)
			requestContext.session ("chatTvPostParams");

		if (params == null) params = new HashMap<String,String> ();

		pf ("<p class=\"links\"><a href=\"%h\">Queues</a>\n",
			postUrl);

		pf ("<a href=\"javascript:top.show_inbox (false);\">Close</a>" +
				"</p>\n");

		pf ("<h2>Reply to user message on screen</h2>\n");

		requestContext.flushNotices (out);

		pf ("<form method=\"post\" action=\"%h\">\n",
			postUrl);

		pf ("<table class=\"details\">\n");

		pf ("<tr> <th>From user code</th> <td>%s</td> </tr>\n",
			sf ("<input type=\"text\" name=\"fromUserCode\" value=\"%h\">",
				Misc.toString (params.get ("fromUserCode"))));

		pf ("<tr> <th>Message</th> <td>%s</td> </tr>\n",
			sf ("<input type=\"text\" name=\"message\" value=\"%h\">",
				Misc.toString (params.get ("message"))));

		pf ("<tr> <th>Actions</th> <td>%s %s</td> </tr>\n",
			sf ("<input type=\"submit\" name=\"postAsTextJockey\" " +
					"value=\"post as text jockey\">"),
			sf ("<input type=\"submit\" name=\"skip\" value=\"skip\">"));

		pf ("</table>\n");

		pf ("</form>\n");
	}
	*/

}