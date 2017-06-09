package wbs.smsapps.broadcast.console;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPost;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;

import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.console.part.AbstractPagePart;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.smsapps.broadcast.model.BroadcastRec;

import wbs.utils.string.FormatWriter;

@Accessors (fluent = true)
@PrototypeComponent ("broadcastNumbersPart")
public
class BroadcastNumbersPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	BroadcastConsoleHelper broadcastHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

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

			goDetails (
				formatWriter);

			goForm (
				formatWriter);

		}

	}

	void goDetails (
			@NonNull FormatWriter formatWriter) {

		htmlTableOpenDetails (
			formatWriter);

		htmlTableDetailsRowWrite (
			formatWriter,
			"Total accepted",
			integerToDecimalString (
				broadcast.getNumAccepted ()));

		htmlTableDetailsRowWrite (
			formatWriter,
			"Total rejected",
			integerToDecimalString (
				broadcast.getNumRejected ()));

		htmlTableClose (
			formatWriter);

	}

	void goForm (
			@NonNull FormatWriter formatWriter) {

		// open form

		htmlFormOpenPost (
			formatWriter);

		// write numbers

		htmlParagraphOpen (
			formatWriter);

		formatWriter.writeLineFormat (
			"Numbers<br>");

		formatWriter.writeLineFormat (
			"<textarea",
			" name=\"numbers\"",
			" rows=\"8\"",
			" cols=\"60\"",
			">%h</textarea>",
			requestContext.parameterOrEmptyString (
				"numbers"));

		htmlParagraphClose (
			formatWriter);

		// write submit buttons

		htmlParagraphOpen (
			formatWriter);

		formatWriter.writeLineFormat (
			"<input",
			" type=\"submit\"",
			" name=\"add\"",
			" value=\"add numbers\"",
			">");

		formatWriter.writeLineFormat (
			"<input",
			" type=\"submit\"",
			" name=\"remove\"",
			" value=\"remove numbers\"",
			">");

		htmlParagraphClose (
			formatWriter);

		// close form

		htmlFormClose (
			formatWriter);

	}

}
