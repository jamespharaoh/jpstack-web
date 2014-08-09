package wbs.apn.chat.user.core.console;

import static wbs.framework.utils.etc.Misc.dateToInstant;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Collection;

import javax.inject.Inject;

import wbs.apn.chat.bill.model.ChatDailyLimitLogObjectHelper;
import wbs.apn.chat.bill.model.ChatDailyLimitLogRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.Html;
import wbs.platform.console.context.ConsoleContext;
import wbs.platform.console.misc.TimeFormatter;
import wbs.platform.console.module.ConsoleManager;
import wbs.platform.console.part.AbstractPagePart;

@PrototypeComponent ("chatUserBillPart")
public
class ChatUserBillPart
	extends AbstractPagePart {

	@Inject
	ChatDailyLimitLogObjectHelper chatDailyLimitLogHelper;

	@Inject
	ConsoleManager consoleManager;

	@Inject
	TimeFormatter timeFormatter;

	private Collection<ChatDailyLimitLogRec> logEntries;

	@Override
	public
	void prepare () {

		logEntries =
			chatDailyLimitLogHelper.findLimitedToday ();

	}

	@Override
	public
	void goHeadStuff () {

	}

	@Override
	public
	void goBodyStuff () {

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th>User</th>\n",
			"<th>Time</th>\n",
			"</tr>");

		ConsoleContext chatUserContext =
			consoleManager.context (
				"chatUser",
				true);

		for (ChatDailyLimitLogRec logEntry
				: logEntries) {

			printFormat (
				"%s",
				Html.magicTr (
					requestContext.resolveContextUrl (
						stringFormat (
							"%s",
							chatUserContext.pathPrefix (),
							"/%u",
							logEntry.getChatUser ().getId ())),
					false));

			printFormat (
				"<td>%h</td>\n",
				logEntry.getChatUser ().getCode (),

				"<td>%h</td>\n",
				timeFormatter.instantToTimestampString (
					dateToInstant (logEntry.getTime ())),

				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

}
