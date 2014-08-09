package wbs.psychic.help.console;

import static wbs.framework.utils.etc.Misc.dateToInstant;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.platform.console.misc.TimeFormatter;
import wbs.platform.console.part.AbstractPagePart;
import wbs.psychic.help.model.PsychicHelpRequestRec;
import wbs.psychic.user.core.console.PsychicUserConsoleHelper;
import wbs.psychic.user.core.model.PsychicUserRec;

@PrototypeComponent ("psychicHelpSummaryPart")
public
class PsychicHelpSummaryPart
	extends AbstractPagePart {

	@Inject
	Database database;

	@Inject
	PsychicUserConsoleHelper psychicUserHelper;

	@Inject
	TimeFormatter timeFormatter;

	PsychicUserRec psychicUser;

	@Override
	public
	void prepare () {

		psychicUser =
			psychicUserHelper.find (
				requestContext.stuffInt ("psychicUserId"));

	}

	@Override
	public
	void goHeadStuff () {

		printFormat (
			"<style>\n",
			"table.list tr.psychicRequestFuture td { background: #ccccff }\n",
			"table.list tr.psychicRequestCurrent td { background: #ffccff }\n",
			"table.list tr.psychicRequestPast td { background: #ccccff }\n",
			"table.list tr.psychicResponse td { background: #ffffcc }\n",
			"</style>\n");

	}

	@Override
	public
	void goBodyStuff () {

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th>Date</th>\n",
			"<th>Time</th>\n",
			"<th>Message</th>\n",
			"<th>User</th>\n",
			"</tr>\n");

		for (
			int index = psychicUser.getNumHelpRequests () - 1;
			index >= 0 && index >= psychicUser.getNumHelpRequests () - 100;
			index --
		) {

			PsychicHelpRequestRec helpRequest =
				psychicUser.getHelpRequestsByIndex ().get (index);

			String rowClass;

			if (index > psychicUser.getNumHelpResponses ()) {
				rowClass = "psychicRequestFuture";
			} else if (index == psychicUser.getNumHelpResponses ()) {
				rowClass = "psychicRequestCurrent";
			} else {
				rowClass = "psychicRequestPast";
			}

			if (helpRequest.getResponseTime () != null) {

				printFormat (
					"<tr class=\"psychicResponse\">\n",

					"<td>%h</td>\n",
					timeFormatter.instantToDateStringShort (
						dateToInstant (
							helpRequest.getResponseTime ().toDate ())),

					"<td>%h</td>\n",
					timeFormatter.instantToTimeString (
						dateToInstant (
							helpRequest.getResponseTime ().toDate ())),

					"<td>%h</td>\n",
					helpRequest.getResponseText ().getText (),

					"<td>%h</td>\n",
					helpRequest.getResponseUser ().getUsername (),

					"</tr>\n");

			}

			printFormat (
				"<tr class=\"%h\">\n",
				rowClass,

				"<td>%h</td>\n",
				timeFormatter.instantToDateStringShort (
					dateToInstant (helpRequest.getRequestTime ().toDate ())),

				"<td>%h</td>\n",
				timeFormatter.instantToTimeString (
					dateToInstant (helpRequest.getRequestTime ().toDate ())),

				"<td>%h</td>\n",
				helpRequest.getRequestText ().getText (),

				"<td></td>\n",

				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

}
