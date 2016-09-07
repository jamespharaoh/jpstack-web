package wbs.smsapps.broadcast.console;

import javax.inject.Inject;

import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.smsapps.broadcast.model.BroadcastRec;

@PrototypeComponent ("broadcastSendPart")
public
class BroadcastSendPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	BroadcastConsoleHelper broadcastHelper;

	@Inject
	UserConsoleLogic userConsoleLogic;

	// state

	BroadcastRec broadcast;

	// implementation

	@Override
	public
	void prepare () {

		broadcast =
			broadcastHelper.findRequired (
				requestContext.stuffInteger (
					"broadcastId"));

	}

	@Override
	public
	void renderHtmlBodyContent () {

		switch (broadcast.getState ()) {

		case cancelled:

			printFormat (
				"<p>This broadcast has been cancelled and can no longer be ",
				"sent.</p>\n");

			break;

		case partiallySent:

			printFormat (
				"<p>This broadcast was partially sent and then cancelled. It ",
				"can no longer be sent.</p>\n");

			break;

		case scheduled:

			printFormat (
				"<p>This broadcast has been scheduled but not yet sent. It ",
				"can be unscheduled or cancelled.</p>\n");

			goDetails ();

			printFormat (
				"<h2>Unschedule</h2>\n");

			printFormat (
				"<p>Unscheduling a broadcast will prevent it from being sent. ",
				"You will be able to add and remove numbers and send or ",
				"schedule it again.</p>\n");

			printFormat (
				"<form method=\"post\">\n",
				"<p><input",
				" type=\"submit\"",
				" name=\"unschedule\"",
				" value=\"unschedule\"",
				"></p>\n",
				"</form>\n");

			printFormat (
				"<h2>Cancel</h2>\n");

			printFormat (
				"<p>Cancelling a broadcast will stop it from being sent, now ",
				"or in the future.</p>\n");

			printFormat (
				"<form method=\"post\">\n",
				"<p><input",
				" type=\"submit\"",
				" name=\"cancel\"",
				" value=\"cancel\"",
				"></p>\n",
				"</form>\n");

			break;

		case sending:

			printFormat (
				"<p>This broadcast is being sent. It can be cancelled.</p>\n");

			goDetails ();

			printFormat (
				"<h2>Cancel</h2>\n");

			printFormat (
				"<p>Cancelling a broadcast will stop the current send and ",
				"prevent it from being sent in the future.</p>\n");

			printFormat (
				"<form method=\"post\">\n",
				"<p><input",
				" type=\"submit\"",
				" name=\"cancel\"",
				" value=\"cancel\"",
				"></p>\n",
				"</form>\n");

			break;

		case sent:

			printFormat (
				"<p>This broadcast has already been sent.</p>\n");

			break;

		case unsent:

			printFormat (
				"<p>This broadcast has not yet been sent. It can be sent now ",
				"or scheduled to automatically sent at a specific time in the ",
				"future. Alternatively, it can be cancelled.</p>\n");

			goDetails ();

			printFormat (
				"<h2>Send now</h2>\n");

			printFormat (
				"<p>Sending a broadcast will begin sending messages ",
				"immediately.</p>\n");

			printFormat (
				"<form method=\"post\">\n",
				"<p><input",
				" type=\"submit\"",
				" name=\"send\"",
				" value=\"send\"",
				"></p>\n",
				"</form>\n");

			printFormat (
				"<h2>Schedule</h2>\n");

			printFormat (
				"<p>Scheduling this broadcast will cause it to be sent ",
				"automatically at the specified time in the future.</p>\n");

			printFormat (
				"<form method=\"post\">\n",

				"<p>Time and date<br>\n",
				"<input",
				" type=\"text\"",
				" name=\"timestamp\"",
				" value=\"%h\"",
				userConsoleLogic.timestampWithTimezoneString (
					transaction.now ()),
				"></p>\n",

				"<p><input",
				" type=\"submit\"",
				" name=\"schedule\"",
				" value=\"schedule\"",
				"></p>\n",

				"</form>\n");

			printFormat (
				"<h2>Cancel</h2>\n");

			printFormat (
				"<p>Cancelling a broadcast will prevent it from being sent in ",
				"the future.</p>\n");

			printFormat (
				"<form method=\"post\">\n",
				"<p><input",
				" type=\"submit\"",
				" name=\"cancel\"",
				" value=\"cancel\"",
				"></p>\n",
				"</form>\n");

			break;

		default:

			throw new RuntimeException ();

		}

	}

	void goDetails () {

		printFormat (
			"<table class=\"details\">\n");

		printFormat (
			"<tr>\n",
			"<th>Message originator</th>\n",
			"<td>%h</td>\n",
			broadcast.getMessageOriginator (),
			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Message text</th>\n",
			"<td>%h</td>\n",
			broadcast.getMessageText (),
			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Number count</th>\n",
			"<td>%h</td>\n",
			broadcast.getNumAccepted (),
			"</tr>\n");

		printFormat (
			"</table>\n");

	}

}
