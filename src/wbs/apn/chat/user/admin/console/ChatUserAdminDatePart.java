package wbs.apn.chat.user.admin.console;

import static wbs.framework.utils.etc.Misc.dateToInstant;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.toStringNull;

import javax.inject.Inject;

import wbs.apn.chat.core.console.ChatUserDateModeConsoleHelper;
import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUserDateLogRec;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.console.misc.TimeFormatter;
import wbs.platform.console.part.AbstractPagePart;

@PrototypeComponent ("chatUserAdminDatePart")
public
class ChatUserAdminDatePart
	extends AbstractPagePart {

	@Inject
	ChatUserConsoleHelper chatUserHelper;

	@Inject
	ChatUserDateModeConsoleHelper chatUserDateModeConsoleHelper;

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	TimeFormatter timeFormatter;

	ChatUserRec chatUser;

	@Override
	public
	void prepare () {

		chatUser =
			chatUserHelper.find (
				requestContext.stuffInt ("chatUserId"));

	}

	@Override
	public
	void goBodyStuff () {

		if (chatUser.getBarred ()) {

			printFormat (
				"<p>This user is barred</p>");

		} else {

			printFormat (
				"<form",
				" method=\"post\"",
				" action=\"%h\"",
				requestContext.resolveLocalUrl (
					"/chatUser.admin.date"),
				">\n");

			printFormat (
				"<table class=\"details\">");

			printFormat (
				"<tr>\n",

				"<th>Date mode</th>\n",

				"<td>%s</td>\n",
				chatUserDateModeConsoleHelper.select (
					"dateMode",
					ifNull (
						requestContext.getForm ("dateMode"),
						toStringNull (
							chatUser.getDateMode ()))),

				"</tr>\n");

			printFormat (
				"<tr>\n",

				"<th>Radius (miles)</th>\n",

				"<td><input",
				" type=\"text\"",
				" name=\"radius\"",
				" value=\"%h\"",
				ifNull (
					requestContext.getForm ("radius"),
					chatUser.getDateRadius ().toString ()),
				"></td>\n",

				"</tr>\n");

			printFormat (
				"<tr>\n",
				"<th>Start hour</th>\n",

				"<td><input",
				" type=\"text\"",
				" name=\"startHour\"",
				" value=\"%h\"",
				ifNull (
					requestContext.getForm ("startHour"),
					chatUser.getDateStartHour ().toString ()),
				"></td>\n",

				"</tr>");

			printFormat (
				"<tr>\n",
				"<th>End hour</th>\n",

				"<td><input",
				" type=\"text\"",
				" name=\"endHour\"",
				" value=\"%h\"",
				ifNull (
					requestContext.getForm ("endHour"),
					chatUser.getDateEndHour ().toString ()),
				"\"></td>\n",

				"</tr>");

			printFormat (
				"<tr>\n",
				"<th>Max profiles per day</th>\n",

				"<td><input",
				" type=\"text\"",
				" name=\"dailyMax\"",
				" value=\"%h\"",
				ifNull (
					requestContext.getForm ("dailyMax"),
					chatUser.getDateDailyMax ().toString ()),
				"></td>\n",

				"</tr>\n");

			printFormat (
				"</table>\n");

			printFormat (
				"<p><input",
				" type=\"submit\"",
				" value=\"save changes\"",
				"></p>\n");

			printFormat (
				"</form>\n");

		}

		printFormat (
			"<table class=\"list\">\n",
			"<tr>\n",
			"<th>Timestamp</th>\n",
			"<th>Source</th>\n",
			"<th>Mode</th>\n",
			"<th>Radius</th>\n",
			"<th>Hours</th>\n",
			"<th>Number</th>\n",
			"</tr>\n");

		for (ChatUserDateLogRec chatUserDateLogRec
				: chatUser.getChatUserDateLogs ()) {

			printFormat (
				"<tr>\n",

				"<td>%h</td>\n",
				ifNull (
					timeFormatter.instantToTimestampString (
						dateToInstant (chatUserDateLogRec.getTimestamp ())),
					"-"));

			if (chatUserDateLogRec.getUser () != null) {

				printFormat (
					"%s\n",
					objectManager.tdForObject (
						chatUserDateLogRec.getUser (),
						null,
						true,
						true));

			} else if (chatUserDateLogRec.getMessage() != null) {

				printFormat (
					"%s\n",
					objectManager.tdForObject (
						chatUserDateLogRec.getMessage (),
						null,
						true,
						true));

			} else {

				printFormat (
					"<td>API</td>\n");

			}

			printFormat (

				"<td>%h</td>\n",
				chatUserDateLogRec.getDateMode (),

				"<td>%h</td>\n",
				chatUserDateLogRec.getRadius (),

				"<td>%h-%h</td>\n",
				chatUserDateLogRec.getStartHour (),
				chatUserDateLogRec.getEndHour (),

				"<td>%h</td>\n",
				chatUserDateLogRec.getDailyMax ());

		}

		printFormat (
			"</table>\n");

	}

}
