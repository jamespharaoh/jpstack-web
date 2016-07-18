package wbs.clients.apn.chat.contact.console;

import static wbs.framework.utils.etc.Misc.isNotPresent;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Set;

import javax.inject.Inject;

import lombok.extern.log4j.Log4j;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

import wbs.clients.apn.chat.contact.model.ChatMonitorInboxObjectHelper;
import wbs.clients.apn.chat.contact.model.ChatMonitorInboxRec;
import wbs.clients.apn.chat.user.core.model.ChatUserAlarmObjectHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserAlarmRec;
import wbs.clients.apn.chat.user.core.model.ChatUserOperatorLabel;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.html.ScriptRef;
import wbs.console.module.ConsoleManager;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.HtmlResponder;
import wbs.framework.application.annotations.PrototypeComponent;

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
				ConsoleApplicationScriptRef.javascript (
					"/js/gsm.js"))

			.add (
				ConsoleApplicationScriptRef.javascript (
					"/js/DOM.js"))

			.build ();

	}

	@Override
	public
	void prepare () {

		Optional<ChatMonitorInboxRec> chatMonitorInboxOptional =
			chatMonitorInboxHelper.find (
				requestContext.stuffInt (
					"chatMonitorInboxId"));

		if (
			isNotPresent (
				chatMonitorInboxOptional)
		) {

			requestContext.addError (
				"Chat monitor inbox item not found");

			log.error (
				stringFormat (
					"Chat monitor inbox not found: %d",
					requestContext.stuffInt (
						"chatMonitorInboxId")));

			return;

		}

		chatMonitorInbox =
			chatMonitorInboxOptional.get ();

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
	void renderHtmlHeadContents () {

		super.renderHtmlHeadContents ();

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
	void renderHtmlBodyContents () {

		requestContext.flushNotices (
			printWriter);

		printFormat (
			"<p",
			" class=\"links\"",
			">\n");

		printFormat (
			"<a",
			" href=\"%h\"",
			requestContext.resolveApplicationUrl (
				"/queues/queue.home"),
			">Queues</a>\n");

		printFormat (
			"<a",
			" href=\"%h\"",
			"javascript:top.show_inbox (false)",
			">Close</a>\n");

		printFormat (
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