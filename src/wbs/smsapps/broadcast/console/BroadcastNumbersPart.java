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

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.smsapps.broadcast.model.BroadcastRec;

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
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContent");

		) {

			goDetails ();

			goForm ();

		}

	}

	void goDetails () {

		htmlTableOpenDetails ();

		htmlTableDetailsRowWrite (
			"Total accepted",
			integerToDecimalString (
				broadcast.getNumAccepted ()));

		htmlTableDetailsRowWrite (
			"Total rejected",
			integerToDecimalString (
				broadcast.getNumRejected ()));

		htmlTableClose ();

	}

	void goForm () {

		// open form

		htmlFormOpenPost ();

		// write numbers

		htmlParagraphOpen ();

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

		htmlParagraphClose ();

		// write submit buttons

		htmlParagraphOpen ();

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

		htmlParagraphClose ();

		// close form

		htmlFormClose ();

	}

}
