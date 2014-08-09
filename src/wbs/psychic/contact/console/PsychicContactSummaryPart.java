package wbs.psychic.contact.console;

import static wbs.framework.utils.etc.Misc.dateToInstant;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.misc.TimeFormatter;
import wbs.platform.console.part.AbstractPagePart;
import wbs.psychic.contact.model.PsychicContactRec;
import wbs.psychic.core.model.PsychicRec;
import wbs.psychic.profile.model.PsychicProfileRec;
import wbs.psychic.request.model.PsychicRequestRec;
import wbs.psychic.user.core.model.PsychicUserRec;

@PrototypeComponent ("psychicContactSummaryPart")
public
class PsychicContactSummaryPart
	extends AbstractPagePart {

	@Inject
	PsychicContactConsoleHelper psychicContactHelper;

	@Inject
	TimeFormatter timeFormatter;

	PsychicContactRec contact;
	PsychicProfileRec profile;
	PsychicUserRec user;
	PsychicRec psychic;

	@Override
	public
	void prepare () {

		contact =
			psychicContactHelper.find (
				requestContext.stuffInt ("psychicContactId"));

		profile =
			contact.getPsychicProfile ();

		user =
			contact.getPsychicUser ();

		psychic =
			user.getPsychic ();

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
			"<table class=\"details\">\n");

		printFormat (
			"<tr>\n",
			"<th>Profile</th>\n",

			"<td>%h</td>\n",
			profile.getName (),

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>User</th>\n",

			"<td>%h</td>\n",
			user.getCode (),

			"</tr>\n");

		printFormat (
			"</table>\n");

		printFormat (
			"<h2>History</h2>\n");

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
			int index = contact.getNumRequests () - 1;
			index >= 0 && index >= contact.getNumRequests () - 100;
			index--) {

			PsychicRequestRec request =
				contact.getRequestsByIndex ().get (index);

			String rowClass;

			if (index > contact.getNumResponses ()) {
				rowClass = "psychicRequestFuture";
			} else if (index == contact.getNumResponses ()) {
				rowClass = "psychicRequestCurrent";
			} else {
				rowClass = "psychicRequestPast";
			}

			if (request.getResponseTime () != null) {

				printFormat (
					"<tr class=\"psychicResponse\">\n",

					"<td>%h</td>\n",
					timeFormatter.instantToDateStringShort (
						dateToInstant (request.getResponseTime ().toDate ())),

					"<td>%h</td>\n",
					timeFormatter.instantToDateStringShort (
						dateToInstant (request.getResponseTime ().toDate ())),

					"<td>%h</td>\n",
					request.getResponseText ().getText (),

					"<td>%h</td>\n",
					request.getUser ().getUsername (),

					"</tr>\n");

			}

			printFormat (
				"<tr class=\"%h\">\n",
				rowClass,

				"<td>%h</td>\n",
				timeFormatter.instantToDateStringShort (
					request.getRequestTime ()),

				"<td>%h</td>\n",
				timeFormatter.instantToDateStringShort (
					request.getRequestTime ()),

				"<td>%h</td>\n",
				request.getRequestText ().getText (),

				"<td></td>\n",

				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

}
