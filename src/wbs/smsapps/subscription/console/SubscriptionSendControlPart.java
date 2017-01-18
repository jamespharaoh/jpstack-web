package wbs.smsapps.subscription.console;

import static wbs.web.utils.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWriteFormat;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPost;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;

import lombok.NonNull;

import org.joda.time.Instant;

import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;

import wbs.platform.user.console.UserConsoleLogic;

import wbs.smsapps.subscription.model.SubscriptionSendRec;

@PrototypeComponent ("subscriptionSendControlPart")
public
class SubscriptionSendControlPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	SubscriptionSendConsoleHelper subscriptionSendHelper;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// state

	SubscriptionSendRec subscriptionSend;

	// implementation

	@Override
	public
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		subscriptionSend =
			subscriptionSendHelper.findFromContextRequired ();

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		goDetails ();

		switch (subscriptionSend.getState ()) {

		case cancelled:

			htmlParagraphWriteFormat (
				"This send has been cancelled and can no longer be sent.");

			break;

		case partiallySent:

			htmlParagraphWriteFormat (
				"This send was partially sent and then cancelled. It can no ",
				"longer be sent.");

			break;

		case scheduled:

			htmlParagraphWriteFormat (
				"This send has been scheduled but not yet sent. It can be ",
				"unscheduled or cancelled.");

			goUnschedule ();
			goCancel ();

			break;

		case sending:

			htmlParagraphWriteFormat (
				"This send is being sent. It can be cancelled.");

			goCancel ();

			break;

		case sent:

			htmlParagraphWriteFormat (
				"This send has already been sent.");

			break;

		case notSent:

			htmlParagraphWriteFormat (
				"This send has not yet been sent. It can be sent now or ",
				"scheduled to automatically sent at a specific time in the ",
				"future. Alternatively, it can be cancelled.");

			goSendNow ();
			goSchedule ();
			goCancel ();

			break;

		default:

			throw new RuntimeException ();

		}

	}

	void goDetails () {

		htmlTableOpenDetails ();

		htmlTableDetailsRowWrite (
			"Description",
			subscriptionSend.getDescription ());

		htmlTableClose ();

	}

	void goSchedule () {

		htmlHeadingTwoWrite (
			"Schedule");

		htmlParagraphWriteFormat (
			"Scheduling this send will cause it to be sent automatically at ",
			"the specified time in the future.");

		// form open

		htmlFormOpenPost ();

		// time and date

		htmlParagraphOpen ();

		formatWriter.writeLineFormat (
			"Time and date<br>");

		formatWriter.writeLineFormat (
			"<input",
			" type=\"text\"",
			" name=\"timestamp\"",
			" value=\"%h\"",
			userConsoleLogic.timestampWithTimezoneString (
				Instant.now ()),
			">");

		htmlParagraphClose ();

		// form controls

		htmlParagraphOpen ();

		formatWriter.writeLineFormat (
			"<input",
			" type=\"submit\"",
			" name=\"schedule\"",
			" value=\"schedule\"",
			">");

		htmlParagraphClose ();

		// form close

		htmlFormClose ();

	}

	void goUnschedule () {

		htmlHeadingTwoWrite (
			"Unschedule");

		htmlParagraphWriteFormat (
			"Unscheduling a send will prevent it from being sent. You will be ",
			"able to add and remove numbers and send or schedule it again");

		htmlFormOpenPost ();

		htmlParagraphOpen ();

		formatWriter.writeLineFormat (
			"<input",
			" type=\"submit\"",
			" name=\"unschedule\"",
			" value=\"unschedule\"",
			">");

		htmlParagraphClose ();

		htmlFormClose ();

	}

	void goSendNow () {

		htmlHeadingTwoWrite (
			"Send now");

		htmlParagraphWriteFormat (
			"Sending a send will begin sending messages immediately.");

		htmlFormOpenPost ();

		htmlParagraphOpen ();

		formatWriter.writeLineFormat (
			"<input",
			" type=\"submit\"",
			" name=\"send\"",
			" value=\"send\"",
			">");

		htmlParagraphClose ();

		htmlFormClose ();

	}

	void goCancel () {

		htmlHeadingTwoWrite (
			"Cancel");

		htmlParagraphWriteFormat (
			"Cancelling a send will stop it from being sent, now or in the ",
			"future.");

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

	}

}
