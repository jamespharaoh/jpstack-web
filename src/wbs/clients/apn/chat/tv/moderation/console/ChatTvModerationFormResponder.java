package wbs.clients.apn.chat.tv.moderation.console;

import wbs.console.responder.HtmlResponder;
import wbs.framework.application.annotations.PrototypeComponent;

@PrototypeComponent ("chatTvModerationFormResponder")
public
class ChatTvModerationFormResponder
	extends HtmlResponder {

	/*
	@Inject
	ChatHelpTemplateConsoleHelper chatHelpTemplateHelper;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	ConsoleManager consoleManager;

	@Inject
	MediaConsoleLogic mediaConsoleLogic;

	ChatTvModerationRec moderation;
	ChatTvMessageRec message;
	ChatUserRec chatUser;

	List<ChatHelpTemplateRec> templates;

	@Override
	protected
	Set<ScriptRef> getScriptRefs () {

		return
			ImmutableSet.<ScriptRef>of (
				JqueryScriptRef.instance,
				new ContextScriptRef ("/js/gsm.js", "text/javascript"),
				new ContextScriptRef ("/js/DOM.js", "text/javascript"),
				new ContextScriptRef ("/js/chat.js", "text/javascript"));

	}

	@Override
	public
	void prepare () {

		moderation =
			chatTvMod.findModerationById (
				requestContext.stuffInt ("chatTvModerationId"));

		if (moderation == null)
			requestContext.addError ("TV moderation queue item does not exist");

		message =
			moderation.getMessage ();

		chatUser =
			message.getChatUser ();

		templates =
			chatHelpTemplateHelper.findByParentAndType (
				chatUser.getChat (),
				"reject_message");

	}

	@Override
	public
	void goHeadStuff () {

		super.goHeadStuff ();

		pf ("<script type=\"text/javascript\">\n");

		// template data

		pf ("var chatHelpTemplates = [];\n");

		for (ChatHelpTemplateRec template : templates) {

			pf ("chatHelpTemplates [%d] = '%j';\n",
				template.getId (),
				template.getText ());

		}

		// show summary part

		pf ("top.show_inbox (true);\n",
			"top.frames ['main'].location = '%j';\n",
			sf ("%s/%d/chatTvModeration_summary",
				consoleManager
					.context ("chatTvModeration")
					.pathPrefix (),
				moderation.getId ()));

		pf ("</script>\n");

	}

	@Override
	public
	void goBodyStuff () {

		pf ("<p class=\"links\"><a href=\"%h\">Queues</a>\n",
			requestContext.contextUrl ("/queues/queue_home"),

			"<a href=\"javascript:top.show_inbox (false);\">Close</a>" +
				"</p>\n");

		if (moderation == null) {
			requestContext.flushNotices (out);
			return;
		}

		pf ("<h2>Moderate user message to screen</h2>\n");

		requestContext.flushNotices (out);

		Context context =
			consoleManager.context ("chatTvModeration");

		String contextPath =
			context.pathPrefix () + "/" + moderation.getId ();

		pf ("<form method=\"post\" action=\"%h\" class=\"chatModForm\">\n",
			requestContext.contextUrl (contextPath + "/chatTvModeration_form"));

		pf ("<table class=\"details\">\n");

		pf ("<tr> <th>User</th> <td>%h</td> </tr>\n",
			chatUser.getPrettyName ());

		pf ("<tr> ",

			"<th>Options</th> ",

			"<td>%s %s</td> ",

				"<input type=\"button\""
					+ " value=\"approve\""
					+ " class=\"showApprove\">",

				"<input type=\"button\""
					+ " value=\"reject\""
					+ " class=\"showReject\">",

			"</tr>\n");

		if (message.getMedia () != null) {

			pf ("<tr> <th>Image</th> <td>%s</td> </tr>\n",
				mediaConsoleLogic.mediaThumb100 (
					message.getMedia ()));

			pf ("<tr class=\"approve hide\"> ",

				"<th>Rotate</th> ",

				"<td>%s</td> ",
				sf ("<select name=\"orient\">",
					"<option value=\"up\">correct</option>",
					"<option value=\"left\">clockwise</option>",
					"<option value=\"right\">anticlockwise</option>",
					"<option value=\"down\">180 degrees</option>",
					"</select>"),

				"</tr>\n");

		}

		pf ("<tr> <th>Original message</th> <td>%h</td> </tr>\n",
			message.getOriginalText ().getText ());

		pf ("<tr class=\"approve hide\"> ",

			"<th>Edited message</th> ",

			"<td>%s</td> ",

				sf ("<textarea name=\"text\""
						+ " cols=\"64\""
						+ " rows=\"4\">"
						+ "%h"
						+ "</textarea>",

					message.getOriginalText ().getText ()),

			"</tr>\n");

		StringBuilder options = new StringBuilder ();
		for (ChatHelpTemplateRec template : templates)
			options .append (sf ("<option value=\"%h\">%h</option>",
				template.getId (), template.getCode ()));

		pf ("<tr class=\"reject hide\"> ",

			"<th>Template</th> ",

			"<td>%s %s</td> ",

				sf ("<select class=\"templateId\">"
						+ "<option>"
						+ "%s"
						+ "</select>",

					options),

				"<input type=\"button\" class=\"templateOk\" value=\"ok\">",

			"</tr>\n");

		pf ("<tr class=\"reject hide\"> ",

			"<th>Message</th> ",

			"<td>%s</td>",

				"<textarea class=\"message\""
					+ " name=\"message\""
					+ " rows=\"4\""
					+ " cols=\"48\">"
					+ "</textarea>",

			" </tr>\n");

		pf ("<tr class=\"approve reject hide\"> ",

			"<th>Actions</th> ",

			"<td>%s %s %s</td> ",

				"<input type=\"submit\""
					+ " class=\"approve hide\""
					+ " name=\"send\""
					+ " value=\"send to screen\">\n",

				"<input type=\"submit\""
					+ " class=\"reject hide\""
					+ " name=\"rejectFree\""
					+ " value=\"reject\">",

				"<input type=\"submit\""
					+ " class=\"reject hide\""
					+ " name=\"rejectBill\""
					+ " value=\"reject and bill\">",

			"</tr>\n");

		pf ("</table>\n");

	}
	*/

}
