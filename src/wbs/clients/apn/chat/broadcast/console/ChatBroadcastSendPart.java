package wbs.clients.apn.chat.broadcast.console;

import static wbs.framework.utils.etc.Misc.emptyStringIfNull;
import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import wbs.clients.apn.chat.core.console.ChatConsoleLogic;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.Html;

@PrototypeComponent ("chatBroadcastSendPart")
public
class ChatBroadcastSendPart
	extends AbstractPagePart {

	@Inject
	ChatConsoleLogic chatConsoleLogic;

	@Override
	public
	void renderHtmlBodyContent () {

		@SuppressWarnings ("unchecked")
		Map<String,String> params =
			(Map<String,String>)
			requestContext.session (
				"chatBroadcastParams");

		if (params == null) {

			params =
				new HashMap<String,String> ();

		}

		printFormat (
			"<form method=\"post\">\n");

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" name=\"verify\"",
			" value=\"verify\"",
			"></p>\n");

		printFormat (
			"<h3>Recipients</h3>\n");

		boolean search =
			equal (
				params.get ("search"),
				"true");

		printFormat (
			"<input",
			" type=\"hidden\"",
			" name=\"search\"",
			" value=\"%h\"",
			search
				? "true"
				: "false",
			">\n");

		if (! search) {

			printFormat (
				"<p><textarea",
				" name=\"numbers\"",
				" cols=\"60\"",
				" rows=\"12\"",
				">%h</textarea></p>\n",
				emptyStringIfNull (
					params.get (
						"numbers")));

			printFormat (
				"<p><input",
				" type=\"submit\"",
				" name=\"searchOn\"",
				" value=\"enable search\"",
				"></p>\n");

			printFormat (
				"<input",
				" type=\"hidden\"",
				" name=\"searchLastActionFrom\"",
				" value=\"%h\"",
				emptyStringIfNull (
					params.get (
						"searchLastActionFrom")),
				">\n");

			printFormat (
				"<input",
				" type=\"hidden\"",
				" name=\"searchLastActionTo\"",
				" value=\"%h\"",
				emptyStringIfNull (
					params.get (
						"lastActionTo")),
				">\n");

			printFormat (
				"<input",
				" type=\"hidden\"",
				" name=\"searchGender\"",
				" value=\"%h\"",
				emptyStringIfNull (
					params.get (
						"searchGender")),
				">\n");

			printFormat (
				"<input",
				" type=\"hidden\"",
				" name=\"searchOrient\"",
				" value=\"%h\"",
				emptyStringIfNull (
					params.get (
						"searchOrient")),
				">\n");

			printFormat (
				"<input",
				" type=\"hidden\"",
				" name=\"searchPicture\"",
				" value=\"%h\"",
				emptyStringIfNull (
					params.get (
						"searchPicture")),
				">\n");

			printFormat (
				"<input",
				" type=\"hidden\"",
				" name=\"searchAdult\"",
				" value=\"%h\"",
				emptyStringIfNull (
					params.get (
						"searchAdult")),
				">\n");

			printFormat (
				"<input",
				" type=\"hidden\"",
				" name=\"searchSpendMin\"",
				" value=\"%h\"",
				emptyStringIfNull (
					params.get (
						"searchSpendMin")),
				">\n");

			printFormat (
				"<input",
				" type=\"hidden\"",
				" name=\"searchSpendMax\"",
				" value=\"%h\"",
				emptyStringIfNull (
					params.get (
						"searchSpendMax")),
				">\n");

		}

		if (search) {

			printFormat (
				"<table class=\"details\">\n");

			printFormat (
				"<tr>\n",
				"<th>Last action from</th>\n",
				"<td>%s</td>\n",
				stringFormat (
					"<input",
					" type=\"text\"",
					" name=\"searchLastActionFrom\"",
					" value=\"%h\"",
					emptyStringIfNull (
						params.get (
							"searchLastActionFrom")),
					">"),
				"</tr>\n");

			printFormat (
				"<tr>\n",
				"<th>Last action to</th>\n",
				"<td>%s</td>\n",
				stringFormat (
					"<input",
					" type=\"text\"",
					" name=\"searchLastActionTo\"",
					" value=\"%h\"",
					emptyStringIfNull (
						params.get (
							"searchLastActionTo")),
					">"),
				"</tr>\n");

			printFormat (
				"<tr>\n",
				"<th>Gender</th>\n",
				"<td>%s</td>\n",
				chatConsoleLogic.selectForGender (
					"searchGender",
					emptyStringIfNull (
						params.get (
							"searchGender"))),
				"</tr>\n");

			printFormat (
				"<tr>\n",
				"<th>Orient</th>\n",
				"<td>%s</td>\n",
				chatConsoleLogic.selectForOrient (
					"searchOrient",
					emptyStringIfNull (
						params.get (
							"searchOrient"))),
				"</tr>\n");

			printFormat (
				"<tr>\n",
				"<th>Picture</th>\n",
				"<td>%s</td>\n",
				Html.selectYesNoMaybe (
					"searchPicture",
					emptyStringIfNull (
						params.get (
							"searchPicture"))),
				"</tr>\n");

			printFormat (
				"<tr>\n",
				"<th>Adult</th>\n",
				"<td>%s</td>\n",
				Html.selectYesNoMaybe (
					"searchAdult",
					emptyStringIfNull (
						params.get (
							"searchAdult"))),
				"</tr>\n");

			printFormat (
				"<tr>\n",
				"<th>Search spend minimum</th>\n",
				"<td>%s</td>\n",
				stringFormat (
					"<input",
					" type=\"text\"",
					" name=\"searchSpendMin\"",
					" value=\"%h\"",
					emptyStringIfNull (
						params.get (
							"searchSpendMin")),
					">"),
				"</tr>\n");

			printFormat (
				"<tr>\n",
				"<th>Search spend maximum</th>\n",
				"<td>%s</td>\n",
				stringFormat (
					"<input",
					" type=\"text\"",
					" name=\"searchSpendMax\"",
					" value=\"%h\"",
					emptyStringIfNull (
						params.get (
							"searchSpendMax")),
					">"),
				"</tr>\n");

			printFormat (
				"</table>\n");

			printFormat (
				"<p><input",
				" type=\"submit\"",
				" name=\"searchOff\"",
				" value=\"disable search\"",
				"></p>\n");

			printFormat (
				"<p><input",
				" type=\"hidden\"",
				" name=\"numbers\"",
				" value=\"%h\"",
				emptyStringIfNull (
					params.get (
						"numbers")),
				">\n");

		}

		if (requestContext.canContext ("chat.manage")) {

			printFormat (
				"<p><input",
				" type=\"checkbox\"",
				" name=\"includeBlocked\"",
				" id=\"includeBlockedCheckbox\"",
				params.get ("includeBlocked") != null
					? " checked"
					: "",
				"><label",
				" for=\"includeBlockedCheckbox\"",
				">include blocked users</label></p>");

			printFormat (
				"<p><input",
				" type=\"checkbox\"",
				" name=\"includeOptedOut\"",
				" id=\"includeOptedOutCheckbox\"",
				params.get ("includeOptedOut") != null
					? " checked"
					: "",
				"><label",
				" for=\"includeOptedOutCheckbox\"",
				">include opted out users</label></p>");

		}

		printFormat (
			"<h3>Message</h3>\n");

		printFormat (
			"<table class=\"details\">\n");

		printFormat (
			"<tr>\n",
			"<th>From user</th>\n",
			"<td><input",
			" type=\"text\"",
			" name=\"fromUserCode\"",
			" value=\"%h\"",
			emptyStringIfNull (
				params.get (
					"fromUserCode")),
			"></tr>\n");

		printFormat (
			"</table>\n");

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" name=\"verify\"",
			" value=\"verify\"",
			"></p>\n");

		printFormat (
			"</form>\n");

	}

}
