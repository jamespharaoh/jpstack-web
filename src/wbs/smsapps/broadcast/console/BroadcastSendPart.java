package wbs.smsapps.broadcast.console;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWriteFormat;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenMethod;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPost;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;

import lombok.NonNull;

import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.platform.user.console.UserConsoleLogic;

import wbs.smsapps.broadcast.model.BroadcastRec;

import wbs.utils.string.FormatWriter;

@PrototypeComponent ("broadcastSendPart")
public
class BroadcastSendPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	BroadcastConsoleHelper broadcastHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// state

	BroadcastRec broadcast;

	// implementation

	@Override
	public
	void prepare (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepare");

		) {

			broadcast =
				broadcastHelper.findFromContextRequired (
					transaction);

		}

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContent");

		) {

			switch (broadcast.getState ()) {

			case cancelled:

				htmlParagraphWriteFormat (
					formatWriter,
					"This broadcast has been cancelled and can no longer be ",
					"sent.");

				break;

			case partiallySent:

				htmlParagraphWriteFormat (
					formatWriter,
					"This broadcast was partially sent and then cancelled. It ",
					"can no longer be sent.");

				break;

			case scheduled:

				htmlParagraphWriteFormat (
					formatWriter,
					"This broadcast has been scheduled but not yet sent. It ",
					"can be unscheduled or cancelled.");

				goDetails (
					formatWriter);

				// unschedule

				htmlHeadingTwoWrite (
					formatWriter,
					"Unschedule");

				formatWriter.writeLineFormat (
					"<p>Unscheduling a broadcast will prevent it from being sent. ",
					"You will be able to add and remove numbers and send or ",
					"schedule it again.</p>");

				htmlFormOpenPost (
					formatWriter);

				htmlParagraphOpen (
					formatWriter);

				formatWriter.writeFormat (
					"<input",
					" type=\"submit\"",
					" name=\"unschedule\"",
					" value=\"unschedule\"",
					">");

				htmlParagraphClose (
					formatWriter);

				htmlFormClose (
					formatWriter);

				// cancel

				htmlHeadingTwoWrite (
					formatWriter,
					"Cancel");

				formatWriter.writeLineFormat (
					"<p>Cancelling a broadcast will stop it from being sent, now ",
					"or in the future.</p>");

				htmlFormOpenPost (
					formatWriter);

				htmlParagraphOpen (
					formatWriter);

				formatWriter.writeLineFormat (
					"<input",
					" type=\"submit\"",
					" name=\"cancel\"",
					" value=\"cancel\"",
					">");

				htmlParagraphClose (
					formatWriter);

				htmlFormClose (
					formatWriter);

				break;

			case sending:

				formatWriter.writeLineFormat (
					"<p>This broadcast is being sent. It can be cancelled.</p>");

				goDetails (
					formatWriter);

				htmlHeadingTwoWrite (
					formatWriter,
					"Cancel");

				formatWriter.writeFormat (
					"<p>Cancelling a broadcast will stop the current send and ",
					"prevent it from being sent in the future.</p>");

				htmlFormOpenPost (
					formatWriter);

				htmlParagraphOpen (
					formatWriter);

				formatWriter.writeFormat (
					"<input",
					" type=\"submit\"",
					" name=\"cancel\"",
					" value=\"cancel\"",
					">");

				htmlParagraphClose (
					formatWriter);

				htmlFormClose (
					formatWriter);

				break;

			case sent:

				formatWriter.writeLineFormat (
					"<p>This broadcast has already been sent.</p>");

				break;

			case unsent:

				formatWriter.writeLineFormat (
					"<p>This broadcast has not yet been sent. It can be sent ",
					"now or scheduled to automatically sent at a specific ",
					"time in the future. Alternatively, it can be ",
					"cancelled.</p>");

				goDetails (
					formatWriter);

				// send broadcast

				htmlHeadingTwoWrite (
					formatWriter,
					"Send now");

				formatWriter.writeFormat (
					"<p>Sending a broadcast will begin sending messages ",
					"immediately.</p>");

				htmlFormOpenMethod (
					formatWriter,
					"post");

				htmlParagraphOpen (
					formatWriter);

				formatWriter.writeLineFormat (
					"<input",
					" type=\"submit\"",
					" name=\"send\"",
					" value=\"send\"",
					">");

				htmlParagraphClose (
					formatWriter);

				htmlFormClose (
					formatWriter);

				// schedule broadcast

				htmlHeadingTwoWrite (
					formatWriter,
					"Schedule");

				formatWriter.writeLineFormat (
					"<p>Scheduling this broadcast will cause it to be sent ",
					"automatically at the specified time in the future.</p>");

				htmlFormOpenMethod (
					formatWriter,
					"post");

				htmlParagraphOpen (
					formatWriter);

				formatWriter.writeLineFormat (
					"Time and date<br>");

				formatWriter.writeLineFormat (
					"<input",
					" type=\"text\"",
					" name=\"timestamp\"",
					" value=\"%h\"",
					userConsoleLogic.timestampWithTimezoneString (
						transaction,
						transaction.now ()),
					">");

				htmlParagraphClose (
					formatWriter);

				htmlParagraphOpen (
					formatWriter);

				formatWriter.writeFormat (
					"<input",
					" type=\"submit\"",
					" name=\"schedule\"",
					" value=\"schedule\"",
					">");

				htmlParagraphClose (
					formatWriter);

				htmlFormClose (
					formatWriter);

				// cancel broadcast

				htmlHeadingTwoWrite (
					formatWriter,
					"Cancel");

				formatWriter.writeLineFormat (
					"<p>Cancelling a broadcast will prevent it from being sent in ",
					"the future.</p>");

				htmlFormOpenMethod (
					formatWriter,
					"post");

				htmlParagraphOpen (
					formatWriter);

				formatWriter.writeFormat (
					"<input",
					" type=\"submit\"",
					" name=\"cancel\"",
					" value=\"cancel\"",
					">");

				htmlParagraphClose (
					formatWriter);

				// close form

				htmlFormClose (
					formatWriter);

				break;

			default:

				throw new RuntimeException ();

			}

		}

	}

	void goDetails (
			@NonNull FormatWriter formatWriter) {

		htmlTableOpenDetails (
			formatWriter);

		htmlTableDetailsRowWrite (
			formatWriter,
			"Message originator",
			broadcast.getMessageOriginator ());

		htmlTableDetailsRowWrite (
			formatWriter,
			"Message text",
			broadcast.getMessageText ());

		htmlTableDetailsRowWrite (
			formatWriter,
			"Number count",
			integerToDecimalString (
				broadcast.getNumAccepted ()));

		htmlTableClose (
			formatWriter);

	}

}
