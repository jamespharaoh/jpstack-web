package wbs.clients.apn.chat.contact.console;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Set;

import javax.inject.Inject;

import lombok.extern.log4j.Log4j;
import wbs.clients.apn.chat.contact.model.ChatMonitorInboxObjectHelper;
import wbs.clients.apn.chat.contact.model.ChatMonitorInboxRec;
import wbs.clients.apn.chat.user.core.model.ChatUserAlarmObjectHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserAlarmRec;
import wbs.clients.apn.chat.user.core.model.ChatUserOperatorLabel;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.context.ConsoleContextScriptRef;
import wbs.platform.console.html.ScriptRef;
import wbs.platform.console.module.ConsoleManager;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.console.responder.HtmlResponder;

import com.google.common.collect.ImmutableSet;

@Log4j
@PrototypeComponent ("chatMonitorInboxFormResponder")
public
class ChatMonitorInboxFormResponder
	extends HtmlResponder {

	@Inject
	ChatMonitorInboxObjectHelper chatMonitorInboxHelper;

	@Inject
	ChatUserAlarmObjectHelper chatUserAlarmHelper;

	@Inject
	ConsoleManager consoleManager;

	@Inject
	ConsoleRequestContext requestContext;

	ChatMonitorInboxRec chatMonitorInbox;
	ChatUserRec userChatUser;
	ChatUserRec monitorChatUser;
	ChatUserAlarmRec alarm;

	@Override
	protected
	Set<ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef>builder ()

			.addAll (
				super.scriptRefs ())

			.add (
				ConsoleContextScriptRef.javascript (
					"/js/gsm.js"))

			.add (
				ConsoleContextScriptRef.javascript (
					"/js/DOM.js"))

			.build ();

	}

	@Override
	public
	void prepare () {

		chatMonitorInbox =
			chatMonitorInboxHelper.find (
				requestContext.stuffInt ("chatMonitorInboxId"));

		if (chatMonitorInbox == null) {

			requestContext.addError (
				"Chat monitor inbox item not found");

			log.error (
				stringFormat (
					"Chat monitor inbox not found: %d",
					requestContext.stuffInt ("chatMonitorInboxId")));

			return;

		}

		userChatUser =
			chatMonitorInbox.getUserChatUser ();

		monitorChatUser =
			chatMonitorInbox.getMonitorChatUser ();

		alarm =
			chatUserAlarmHelper.find (
				userChatUser,
				monitorChatUser);

	}

	@Override
	public
	void goHeadStuff () {

		super.goHeadStuff ();

		if (chatMonitorInbox == null)
			return;

		printFormat (
			"<script language=\"javascript\">\n",

			"top.show_inbox (true);\n",

			"top.frames['main'].location = '%j';\n",
			requestContext.resolveApplicationUrl (
				stringFormat (
					"%s",
					consoleManager
						.context (
							"chatMonitorInbox",
							true)
						.pathPrefix (),
					"/%u",
					chatMonitorInbox.getId (),
					"/chatMonitorInbox.summary")),

			"</script>\n");

		if (
			userChatUser.getOperatorLabel ()
				== ChatUserOperatorLabel.operator
		) {

			printFormat (
				"<style type=\"text/css\">\n",
				"h2 { background: #800000; }\n",
				"table.details th { background: #800000; }\n",
				"</style>\n");

		}

	}

	String doName (
			ChatUserRec chatUser) {

		if (chatUser.getName () != null) {
			return chatUser.getName () + " " + chatUser.getCode ();
		} else {
			return chatUser.getCode ();
		}

	}

	@Override
	public
	void goBodyStuff () {

		requestContext.flushNotices (out);

		printFormat (
			"<p",
			" class=\"links\"",
			">\n",

			"<a",
			" href=\"%h\"",
			requestContext.resolveApplicationUrl (
				"/queues/queue.home"),
			">Queues</a>\n",

			"<a",
			" href=\"%h\"",
			"javascript:top.show_inbox (false)",
			">Close</a>\n",

			"</p>\n");

		if (chatMonitorInbox == null)
			return;

		printFormat (
			"<h2>Send message as %h</h2>\n",
			userChatUser.getOperatorLabel ());

		printFormat (
			"<form",
			" method=\"post\"",

			" action=\"%h\"",
			requestContext.resolveApplicationUrl (
				stringFormat (
					"%s",
					consoleManager
						.context (
							"chatMonitorInbox",
							true)
						.pathPrefix (),
					"/%u",
					chatMonitorInbox.getId (),
					"/chatMonitorInbox.form")),

			">\n");

		printFormat (
			"<table class=\"details\">\n");

		printFormat (
			"<tr>\n",
			"<th>From</th>\n",

			"<td>%h</td>\n",
			doName (monitorChatUser),

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>To</th>\n",

			"<td>%h</td>\n",
			doName (userChatUser),

			"</tr>\n");

		printFormat (
			"<tr>\n",

			"<th>Message</th>\n",

			"<td",
			" colspan=\"2\"",
			"><textarea",
			" name=\"text\"",
			" cols=\"64\"",
			" rows=\"4\"",
			" onkeyup=\"%h\"",
			stringFormat (
				"gsmCharCountMultiple2 (this, %s, %d);",
				"document.getElementById ('chars')",
				ChatMonitorInboxConsoleLogic.SINGLE_MESSAGE_LENGTH
					* ChatMonitorInboxConsoleLogic.MAX_OUT_MONITOR_MESSAGES),
			" onfocus=\"%h\"",
			stringFormat (
				"gsmCharCountMultiple2 (this, %s, %d);",
				"document.getElementById ('chars')",
				ChatMonitorInboxConsoleLogic.SINGLE_MESSAGE_LENGTH
					* ChatMonitorInboxConsoleLogic.MAX_OUT_MONITOR_MESSAGES),
			"></textarea></td>\n",

			"</tr>\n");

		printFormat (
			"<tr>\n",

			"<th>Chars</th>\n",

			"<td>%s %s</td>\n",
			"<span id=\"chars\">&nbsp;</span>",
			"<span id=\"messageCount\">&nbsp;</span>",

			"</tr>\n");

		printFormat (
			"</table>\n");

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" name=\"send\"",
			" value=\"send message\"",
			" onclick=\"%h\"",
			alarm == null
				? stringFormat (
					"return confirm ('%j');",
					"send message with no alarm (please ignore this message " +
					"if you already set one)")
				: "",
			">\n");

		printFormat (
			"<input",
			" type=\"submit\"",
			" name=\"sendAndNote\"",
			" value=\"send and make note\"",
			" onclick=\"%h\"",
			alarm == null
				? stringFormat (
					"return confirm ('%j');",
					"send message with no alarm (please ignore this message " +
					"if you already set one)")
				: "",
			">\n");

		printFormat (
			"<input",
			" type=\"submit\"",
			" name=\"ignore\"",
			" value=\"don't send anything\"",
			" onclick=\"%h\"",
			alarm == null
				? stringFormat (
					"return confirm ('%j');",
					"ignore message with no alarm (please ignore this " +
					"message if you already set one)")
				: "",
			"></p>\n");

		printFormat (
			"</form>\n");

	}

}