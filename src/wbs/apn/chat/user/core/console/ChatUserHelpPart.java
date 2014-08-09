package wbs.apn.chat.user.core.console;

import static wbs.framework.utils.etc.Misc.dateToInstant;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Calendar;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import wbs.apn.chat.help.model.ChatHelpLogRec;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.Html;
import wbs.platform.console.misc.TimeFormatter;
import wbs.platform.console.part.AbstractPagePart;
import wbs.sms.message.core.console.MessageConsoleStuff;

@PrototypeComponent ("chatUserHelpPart")
public
class ChatUserHelpPart
	extends AbstractPagePart {

	@Inject
	ChatUserConsoleHelper chatUserHelper;

	@Inject
	TimeFormatter timeFormatter;

	ChatUserRec chatUser;
	Set<ChatHelpLogRec> helps;

	@Override
	public
	void prepare () {

		chatUser =
			chatUserHelper.find (
				requestContext.stuffInt ("chatUserId"));

		helps =
			new TreeSet<ChatHelpLogRec> (
				chatUser.getChatHelpLogs ());

	}

	@Override
	public
	void goBodyStuff () {

		String link =
			requestContext.resolveLocalUrl (
				"/chatUser.helpForm");

		printFormat (
			"<p><button onclick=\"%h\">send message</button></p>\n",
			stringFormat (
				"top.frames['inbox'].location='%j';",
				link));

		if (helps.size () == 0) {

			printFormat (
				"<p>No history to display.</p>\n");

			return;

		}

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th>&nbsp;</th>\n",
			"<th>Time</th>\n",
			"<th>Message</th>\n",
			"<th>Our number</th>\n",
			"<th>User</th>\n",
			"</tr>\n");

		int dayNumber = 0;

		Calendar calendar =
			Calendar.getInstance ();

		for (ChatHelpLogRec help : helps) {

			calendar.setTime (
				help.getTimestamp ());

			int newDayNumber =
				+ (calendar.get (Calendar.YEAR) << 9)
				+ calendar.get (Calendar.DAY_OF_YEAR);

			if (newDayNumber != dayNumber) {

				dayNumber =
					newDayNumber;

				printFormat (
					"<tr class=\"sep\">\n");

				printFormat (
					"<tr style=\"font-weight: bold\">\n",

					"<td colspan=\"5\">%h</td>\n",
					timeFormatter.instantToDateStringLong (
						dateToInstant (help.getTimestamp ())),

					"</tr>\n");

			}

			String rowClass =
				MessageConsoleStuff.classForMessageDirection (
					help.getDirection ());

			printFormat (
				"<tr class=\"%h\">\n",
				rowClass,

				"<td style=\"background: %h\">&nbsp;</td>\n",
				Html.genHtmlColor (
					ifNull (
						help.getOurNumber (),
						0)),

				"<td>%h</td>\n",
				timeFormatter.instantToTimeString (
					dateToInstant (help.getTimestamp ())),

				"<td>%h</td>\n",
				help.getText (),

				"<td>%h</td>\n",
				help.getOurNumber (),

				"<td>%h</td>\n",
				help.getUser () == null
					? ""
					: help.getUser ().getUsername (),

				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

}
