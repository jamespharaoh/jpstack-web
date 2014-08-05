package wbs.apn.chat.broadcast.console;

import static wbs.framework.utils.etc.Misc.dateToInstant;
import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.in;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import wbs.apn.chat.core.console.ChatConsoleHelper;
import wbs.apn.chat.core.logic.ChatMiscLogic;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.platform.console.context.ConsoleContextScriptRef;
import wbs.platform.console.html.ScriptRef;
import wbs.platform.console.misc.TimeFormatter;
import wbs.platform.console.part.AbstractPagePart;
import wbs.sms.gsm.Gsm;

import com.google.common.collect.ImmutableSet;

@PrototypeComponent ("chatBroadcastVerifyPart")
public
class ChatBroadcastVerifyPart
	extends AbstractPagePart {

	@Inject
	ChatConsoleHelper chatHelper;

	@Inject
	ChatMiscLogic chatMiscLogic;

	@Inject
	ChatUserConsoleHelper chatUserHelper;

	@Inject
	Database database;

	@Inject
	TimeFormatter timeFormatter;

	Map<String,String> params;

	ChatRec chat;
	ChatUserRec fromUser;

	boolean search;

	@Override
	public
	void prepare () {

		@SuppressWarnings ("unchecked")
		Map<String,String> paramsTemp =
			(Map<String,String>)
			requestContext.request ("chatBroadcastParams");

		params =
			paramsTemp;

		chat =
			chatHelper.find (
				requestContext.stuffInt ("chatId"));

		fromUser =
			chatUserHelper.findByCode (
				chat,
				params.get ("fromUserCode"));

		search =
			equal (
				requestContext.getForm ("search"),
				"true");

	}

	@Override
	public
	Set<ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef>builder ()

			.addAll (
				super.scriptRefs ())

			.add (
				ConsoleContextScriptRef.javascript (
					"/js/jquery-1.7.1.js"))

			.add (
				ConsoleContextScriptRef.javascript (
					"/js/DOM.js"))

			.add (
				ConsoleContextScriptRef.javascript (
					"/js/gsm.js"))

			.build ();

	}

	@Override
	public
	void goBodyStuff () {

		// form

		printFormat (
			"<form method=\"post\">\n");

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" name=\"send\"",
			" value=\"send\"",
			">\n",

			"<input",
			" type=\"submit\"",
			" name=\"back\"",
			" value=\"back\"",
			"></p>\n");

		for (Map.Entry<String,String> entry
				: params.entrySet ()) {

			if (
				in (
					entry.getKey (),
					"verify",
					"message")
			) {
				continue;
			}

			printFormat (
				"<input",
				" type=\"hidden\"",
				" name=\"%h\"",
				entry.getKey (),
				" value=\"%h\"",
				entry.getValue (),
				">\n");

		}

		// message info

		printFormat (
			"<h3>Message</h3>\n");

		printFormat (
			"<table class=\"details\">\n");

		printFormat (
			"<tr>\n",
			"<th>From user code</th>\n",
			"<td>%h</td>\n",
			fromUser.getCode (),
			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>From user name</th>\n",
			"<td>%h</td>\n",
			fromUser.getName (),
			"</tr>\n");

		printFormat (
			"<tr> <th>From user info</th> <td>%h</td> </tr>\n",
			fromUser.getInfoText () != null
				? fromUser.getInfoText ().getText ()
				: "-");

		String prefix =
			fromUser.getName () != null
				? stringFormat (
					"From %s %s: ",
					fromUser.getName (),
					fromUser.getCode ())
				: stringFormat (
					"From %s: ",
					fromUser.getCode ());

		String charCountId =
			stringFormat (
				"gsmCharCount-%d",
				requestContext.requestUnique ());

		printFormat (
			"<tr>\n",
			"<th>Message</th>\n",
			"<td>%s</td>\n",
			stringFormat (
				"%h (%s)<br>%s",
				prefix,
				stringFormat (
					"<span id=\"%h\">-</span>",
					charCountId),
				stringFormat (
					"<textarea",
					" class=\"gsmCharCount\"",
					" name=\"message\"",
					" rows=\"6\"",
					" cols=\"60\"",
					" data-char-count-max=\"%h\"",
					160 - Gsm.length (prefix),
					" data-char-count-id=\"%h\">",
					charCountId,
					"%h</textarea>",
					params.get ("message"))),
			"</tr>\n");

		printFormat (
			"</table>\n");

		printFormat (
			"<input",
			" type=\"hidden\"",
			" name=\"prefix\"",
			" value=\"%h\"",
			prefix,
			">\n");

		printFormat (
			"</form>\n");

		// recipients info

		printFormat (
			"<h3>Recipients</h3>\n");

		@SuppressWarnings ("unchecked")
		List<Integer> chatUserIds =
			(List<Integer>)
			requestContext.request ("chatBroadcastChatUserIds");

		printFormat (
			"<p>%d recipients in total.</p>\n",
			chatUserIds.size ());

		if (search) {

			printFormat (
				"<p>The actual number of recipients may change slightly on ",
				"send as the search will be performed again.</p>\n");

		}

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th>Number</th>\n",
			"<th>Code</th>\n",
			"<th>Name</th>\n",
			"<th>Last action</th>\n",
			"<th>Gender</th>\n",
			"<th>Orient</th>\n",
			"<th>Pic</th>\n",
			"<th>Adult</th>\n",
			"</tr>\n");

		int loop = 0;

		for (Integer chatUserId
				: chatUserIds) {

			ChatUserRec chatUser =
				chatUserHelper.find (chatUserId);

			printFormat (
				"<tr>\n",

				"<td>%h</td>\n",
				chatUser.getNumber ().getNumber (),

				"<td>%h</td>\n",
				chatUser.getCode (),

				"<td>%h</td>\n",
				chatUser.getName (),

				"<td>%h</td>\n",
				timeFormatter.instantToTimestampString (
					chatMiscLogic.timezone (
						chat),
					dateToInstant (
						chatUser.getLastAction ())),

				"<td>%h</td>\n",
				chatUser.getGender (),

				"<td>%h</td>\n",
				chatUser.getOrient (),

				"<td>%h</td>\n",
				chatUser.getMainChatUserImage () != null
					? "yes"
					: "no",

				"<td>%h</td>\n",
				chatUser.getAdultVerified ()
					? "yes"
					: "no",

				"</tr>\n");

			if (++ loop % 128 == 0) {

				database.clear ();

			}

		}

		printFormat (
			"</table>\n");

	}

}
