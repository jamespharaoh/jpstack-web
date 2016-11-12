package wbs.smsapps.broadcast.console;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenMethod;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPost;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;

import lombok.NonNull;

import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.smsapps.broadcast.model.BroadcastRec;

@PrototypeComponent ("broadcastSendPart")
public
class BroadcastSendPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	BroadcastConsoleHelper broadcastHelper;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// state

	BroadcastRec broadcast;

	// implementation

	@Override
	public
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		broadcast =
			broadcastHelper.findRequired (
				requestContext.stuffInteger (
					"broadcastId"));

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		switch (broadcast.getState ()) {

		case cancelled:

			formatWriter.writeLineFormat (
				"<p>This broadcast has been cancelled and can no longer be ",
				"sent.</p>");

			break;

		case partiallySent:

			formatWriter.writeLineFormat (
				"<p>This broadcast was partially sent and then cancelled. It ",
				"can no longer be sent.</p>");

			break;

		case scheduled:

			formatWriter.writeLineFormat (
				"<p>This broadcast has been scheduled but not yet sent. It ",
				"can be unscheduled or cancelled.</p>");

			goDetails ();

			// unschedule

			htmlHeadingTwoWrite (
				"Unschedule");

			formatWriter.writeLineFormat (
				"<p>Unscheduling a broadcast will prevent it from being sent. ",
				"You will be able to add and remove numbers and send or ",
				"schedule it again.</p>");

			htmlFormOpenPost ();

			htmlParagraphOpen ();

			formatWriter.writeFormat (
				"<input",
				" type=\"submit\"",
				" name=\"unschedule\"",
				" value=\"unschedule\"",
				">");

			htmlParagraphClose ();

			htmlFormClose ();

			// cancel

			htmlHeadingTwoWrite (
				"Cancel");

			formatWriter.writeLineFormat (
				"<p>Cancelling a broadcast will stop it from being sent, now ",
				"or in the future.</p>");

			htmlFormOpenPost ();

			htmlParagraphOpen ();

			formatWriter.writeLineFormat (
				"<input",
				" type=\"submit\"",
				" name=\"cancel\"",
				" value=\"cancel\"",
				">");

			htmlParagraphClose ();

			htmlFormClose ();

			break;

		case sending:

			formatWriter.writeLineFormat (
				"<p>This broadcast is being sent. It can be cancelled.</p>");

			goDetails ();

			htmlHeadingTwoWrite (
				"Cancel");

			formatWriter.writeFormat (
				"<p>Cancelling a broadcast will stop the current send and ",
				"prevent it from being sent in the future.</p>");

			htmlFormOpenPost ();

			htmlParagraphOpen ();

			formatWriter.writeFormat (
				"<input",
				" type=\"submit\"",
				" name=\"cancel\"",
				" value=\"cancel\"",
				">");

			htmlParagraphClose ();

			htmlFormClose ();

			break;

		case sent:

			formatWriter.writeLineFormat (
				"<p>This broadcast has already been sent.</p>");

			break;

		case unsent:

			formatWriter.writeLineFormat (
				"<p>This broadcast has not yet been sent. It can be sent now ",
				"or scheduled to automatically sent at a specific time in the ",
				"future. Alternatively, it can be cancelled.</p>");

			goDetails ();

			// send broadcast

			htmlHeadingTwoWrite (
				"Send now");

			formatWriter.writeFormat (
				"<p>Sending a broadcast will begin sending messages ",
				"immediately.</p>");

			htmlFormOpenMethod (
				"post");

			htmlParagraphOpen ();

			formatWriter.writeLineFormat (
				"<input",
				" type=\"submit\"",
				" name=\"send\"",
				" value=\"send\"",
				">");

			htmlParagraphClose ();

			htmlFormClose ();

			// schedule broadcast

			htmlHeadingTwoWrite (
				"Schedule");

			formatWriter.writeLineFormat (
				"<p>Scheduling this broadcast will cause it to be sent ",
				"automatically at the specified time in the future.</p>");

			htmlFormOpenMethod (
				"post");

			htmlParagraphOpen ();

			formatWriter.writeLineFormat (
				"Time and date<br>");

			formatWriter.writeLineFormat (
				"<input",
				" type=\"text\"",
				" name=\"timestamp\"",
				" value=\"%h\"",
				userConsoleLogic.timestampWithTimezoneString (
					transaction.now ()),
				">");

			htmlParagraphClose ();

			htmlParagraphOpen ();

			formatWriter.writeFormat (
				"<input",
				" type=\"submit\"",
				" name=\"schedule\"",
				" value=\"schedule\"",
				">");

			htmlParagraphClose ();

			htmlFormClose ();

			// cancel broadcast

			htmlHeadingTwoWrite (
				"Cancel");

			formatWriter.writeLineFormat (
				"<p>Cancelling a broadcast will prevent it from being sent in ",
				"the future.</p>");

			htmlFormOpenMethod (
				"post");

			htmlParagraphOpen ();

			formatWriter.writeFormat (
				"<input",
				" type=\"submit\"",
				" name=\"cancel\"",
				" value=\"cancel\"",
				">");

			htmlParagraphClose ();

			// close form

			htmlFormClose ();

			break;

		default:

			throw new RuntimeException ();

		}

	}

	void goDetails () {

		htmlTableOpenDetails ();

		htmlTableDetailsRowWrite (
			"Message originator",
			broadcast.getMessageOriginator ());

		htmlTableDetailsRowWrite (
			"Message text",
			broadcast.getMessageText ());

		htmlTableDetailsRowWrite (
			"Number count",
			integerToDecimalString (
				broadcast.getNumAccepted ()));

		htmlTableClose ();

	}

}
