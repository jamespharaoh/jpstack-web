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

import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.platform.user.console.UserConsoleLogic;

import wbs.smsapps.subscription.model.SubscriptionSendRec;

import wbs.utils.string.FormatWriter;

@PrototypeComponent ("subscriptionSendControlPart")
public
class SubscriptionSendControlPart
	extends AbstractPagePart {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

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
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepare");

		) {

			subscriptionSend =
				subscriptionSendHelper.findFromContextRequired (
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

			goDetails (
				transaction,
				formatWriter);

			switch (subscriptionSend.getState ()) {

			case cancelled:

				htmlParagraphWriteFormat (
					formatWriter,
					"This send has been cancelled and can no longer be sent.");

				break;

			case partiallySent:

				htmlParagraphWriteFormat (
					formatWriter,
					"This send was partially sent and then cancelled. It can ",
					"no longer be sent.");

				break;

			case scheduled:

				htmlParagraphWriteFormat (
					formatWriter,
					"This send has been scheduled but not yet sent. It can be ",
					"unscheduled or cancelled.");

				goUnschedule (
					transaction,
					formatWriter);

				goCancel (
					transaction,
					formatWriter);

				break;

			case sending:

				htmlParagraphWriteFormat (
					formatWriter,
					"This send is being sent. It can be cancelled.");

				goCancel (
					transaction,
					formatWriter);

				break;

			case sent:

				htmlParagraphWriteFormat (
					formatWriter,
					"This send has already been sent.");

				break;

			case notSent:

				htmlParagraphWriteFormat (
					formatWriter,
					"This send has not yet been sent. It can be sent now or ",
					"scheduled to automatically sent at a specific time in ",
					"the future. Alternatively, it can be cancelled.");

				goSendNow (
					transaction,
					formatWriter);

				goSchedule (
					transaction,
					formatWriter);

				goCancel (
					transaction,
					formatWriter);

				break;

			default:

				throw new RuntimeException ();

			}

		}

	}

	void goDetails (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"goDetails");

		) {

			htmlTableOpenDetails (
				formatWriter);

			htmlTableDetailsRowWrite (
				formatWriter,
				"Description",
				subscriptionSend.getDescription ());

			htmlTableClose (
				formatWriter);

		}

	}

	void goSchedule (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"goSchedule");

		) {

			htmlHeadingTwoWrite (
				formatWriter,
				"Schedule");

			htmlParagraphWriteFormat (
				formatWriter,
				"Scheduling this send will cause it to be sent automatically ",
				"at the specified time in the future.");

			// form open

			htmlFormOpenPost (
				formatWriter);

			// time and date

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

			// form controls

			htmlParagraphOpen (
				formatWriter);

			formatWriter.writeLineFormat (
				"<input",
				" type=\"submit\"",
				" name=\"schedule\"",
				" value=\"schedule\"",
				">");

			htmlParagraphClose (
				formatWriter);

			// form close

			htmlFormClose (
				formatWriter);

		}

	}

	void goUnschedule (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"goUnschedule");

		) {

			htmlHeadingTwoWrite (
				formatWriter,
				"Unschedule");

			htmlParagraphWriteFormat (
				formatWriter,
				"Unscheduling a send will prevent it from being sent. You ",
				"will be able to add and remove numbers and send or schedule ",
				"it again");

			htmlFormOpenPost (
				formatWriter);

			htmlParagraphOpen (
				formatWriter);

			formatWriter.writeLineFormat (
				"<input",
				" type=\"submit\"",
				" name=\"unschedule\"",
				" value=\"unschedule\"",
				">");

			htmlParagraphClose (
				formatWriter);

			htmlFormClose (
				formatWriter);

		}

	}

	void goSendNow (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"goSendNow");

		) {

			htmlHeadingTwoWrite (
				formatWriter,
				"Send now");

			htmlParagraphWriteFormat (
				formatWriter,
				"Sending a send will begin sending messages immediately.");

			htmlFormOpenPost (
				formatWriter);

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

		}

	}

	void goCancel (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"goCancel");

		) {

			htmlHeadingTwoWrite (
				formatWriter,
				"Cancel");

			htmlParagraphWriteFormat (
				formatWriter,
				"Cancelling a send will stop it from being sent, now or in ",
				"the future.");

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

		}

	}

}
