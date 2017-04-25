package wbs.platform.status.console;

import static wbs.web.utils.HtmlAttributeUtils.htmlAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlIdAttribute;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenList;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import lombok.NonNull;

import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.html.HtmlLink;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.part.PagePart;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.ConsoleHtmlResponder;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("statusResponder")
public
class StatusResponder
	extends ConsoleHtmlResponder {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	StatusLineManager statusLineManager;

	// state

	List <PagePart> pageParts =
		new ArrayList<> ();

	// details

	@Override
	protected
	Set <ScriptRef> myScriptRefs () {

		return ImmutableSet.<ScriptRef> builder ()

			.add (
				JqueryScriptRef.instance)

			.add (
				ConsoleApplicationScriptRef.javascript (
					"/js/js-cookie-2.0.4.js"))

			.add (
				ConsoleApplicationScriptRef.javascript (
					"/js/async.js"))

			.add (
				ConsoleApplicationScriptRef.javascript (
					"/js/status.js"))

			.addAll (
				pageParts.stream ()

				.map (
					PagePart::scriptRefs)

				.flatMap (
					Set::stream)

				.iterator ())

			.build ();

	}

	@Override
	protected
	Set <HtmlLink> myHtmlLinks () {

		return ImmutableSet.<HtmlLink> of (

			HtmlLink.applicationCssStyle (
				"/style/status.css")

		);

	}

	// implementation

	@Override
	protected
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"setup");

		super.setup (
			taskLogger);

		for (
			StatusLine statusLine
				: statusLineManager.getStatusLines ()
		) {

			PagePart pagePart =
				statusLine.createPagePart (
					taskLogger);

			pagePart.setup (
				taskLogger,
				Collections.emptyMap ());

			pageParts.add (
				pagePart);

		}

	}

	@Override
	protected
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"prepare");

		super.prepare (
			taskLogger);

		pageParts.forEach (
			pagePart ->
				pagePart.prepare (
					taskLogger));

	}

	@Override
	protected
	void renderHtmlHeadContents (
			@NonNull TaskLogger parentTaskLogger) {

		super.renderHtmlHeadContents (
			parentTaskLogger);

		pageParts.forEach (
			pagePart ->
				pagePart.renderHtmlHeadContent (
					parentTaskLogger));

	}

	@Override
	protected
	void renderHtmlBody (
			@NonNull TaskLogger parentTaskLogger) {

		formatWriter.writeLineFormat (
			"<body>");

		formatWriter.increaseIndent ();

		renderHtmlBodyContents (
			parentTaskLogger);

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"</body>");

	}

	@Override
	protected
	void renderHtmlBodyContents (
			@NonNull TaskLogger parentTaskLogger) {

		// table open

		htmlTableOpenList (

			htmlIdAttribute (
				"statusTable"),

			htmlAttribute (
				"width",
				"100%")

		);

		// heading row

		htmlTableRowOpen ();

		htmlTableHeaderCellWrite (
			"Status",
			htmlIdAttribute (
				"headerCell"));

		htmlTableRowClose ();

		// loading row

		htmlTableRowOpen (
			htmlIdAttribute (
				"loadingRow"));

		htmlTableCellWrite (
			"Loading...",
			htmlIdAttribute (
				"loadingCell"));

		htmlTableRowClose ();

		// notice row

		htmlTableRowOpen (
			htmlIdAttribute (
				"noticeRow"));

		htmlTableCellWrite (
			"—",
			htmlIdAttribute (
				"noticeCell"));

		htmlTableRowClose ();

		// time row

		htmlTableRowOpen (
			htmlIdAttribute (
				"timeRow"));

		htmlTableCellWrite (
			"—",
			htmlIdAttribute (
				"timeCell"));

		htmlTableRowClose ();

		// parts

		pageParts.forEach (
			pagePart ->
				pagePart.renderHtmlBodyContent (
					parentTaskLogger));

		// log out row

		htmlTableRowOpen ();

		htmlTableCellOpen ();

		htmlFormOpenPostAction (
			"logoff");

		formatWriter.writeLineFormat (
			"<input",
			" type=\"submit\"",
			" value=\"log out\"",
			">");

		htmlFormClose ();

		htmlTableCellClose ();

		htmlTableRowClose ();

		// table close

		htmlTableClose ();

	}

}
