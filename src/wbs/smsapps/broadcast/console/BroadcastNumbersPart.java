package wbs.smsapps.broadcast.console;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableDetailsRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenDetails;
import static wbs.utils.web.HtmlUtils.htmlFormClose;
import static wbs.utils.web.HtmlUtils.htmlFormOpenMethod;
import static wbs.utils.web.HtmlUtils.htmlParagraphClose;
import static wbs.utils.web.HtmlUtils.htmlParagraphOpen;

import lombok.experimental.Accessors;

import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.smsapps.broadcast.model.BroadcastRec;

@Accessors (fluent = true)
@PrototypeComponent ("broadcastNumbersPart")
public
class BroadcastNumbersPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	BroadcastConsoleHelper broadcastHelper;

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

		goDetails ();

		goForm ();

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

		htmlFormOpenMethod (
			"post");

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
