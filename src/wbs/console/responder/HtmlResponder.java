package wbs.console.responder;

import static wbs.utils.collection.CollectionUtils.listSorted;
import static wbs.utils.etc.EnumUtils.enumName;
import static wbs.utils.etc.LogicUtils.booleanToYesNo;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import lombok.NonNull;

import org.joda.time.Instant;

import wbs.console.html.HtmlLink;
import wbs.console.html.ScriptRef;
import wbs.console.priv.UserPrivChecker;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogEvent;
import wbs.framework.logging.TaskLogger;

import wbs.utils.time.TimeFormatter;

public abstract
class HtmlResponder
	extends ConsolePrintResponder {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
	TimeFormatter timeFormatter;

	// details

	protected
	String getTitle () {
		return "Untitled";
	}

	protected
	Set <ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef> builder ()

			.addAll (
				myScriptRefs ())

			.build ();

	}

	protected
	Set <ScriptRef> myScriptRefs () {

		return ImmutableSet.of ();

	}

	protected
	Set <HtmlLink> htmlLinks () {

		return ImmutableSet.<HtmlLink> builder ()

			.add (
				HtmlLink.applicationCssStyle (
					"/style/basic.css"))

			.add (
				HtmlLink.applicationIcon (
					"/favicon.ico"))

			.add (
				HtmlLink.applicationShortcutIcon (
					"/favicon.ico"))

			.addAll (
				myHtmlLinks ())

			.build ();

	}

	protected
	Set <HtmlLink> myHtmlLinks () {

		return ImmutableSet.of ();

	}

	@Override
	protected
	void setHtmlHeaders ()
		throws IOException {

		super.setHtmlHeaders ();

		requestContext.setHeader (
			"Content-Type",
			"text/html; charset=utf-8");

		requestContext.setHeader (
			"Cache-Control",
			"no-cache");

		requestContext.setHeader (
			"Expiry",
			timeFormatter.httpTimestampString (
				Instant.now ()));

	}

	protected
	void renderHtmlDoctype () {

		formatWriter.writeLineFormat (
			"<!DOCTYPE html>");

	}

	protected
	void renderHtmlStyleSheets () {

		formatWriter.writeLineFormat (
			"<link",
			" rel=\"stylesheet\"",
			" href=\"%h\"",
			requestContext.resolveApplicationUrl (
				"/style/basic.css"),
			">");

	}

	protected
	void goMetaRefresh () {
	}

	protected
	void goMeta () {
		goMetaRefresh ();
	}

	protected
	void renderHtmlTitle () {

		formatWriter.writeLineFormat (
			"<title>%h</title>",
			getTitle ());

	}

	protected
	void renderHtmlScriptRefs () {

		for (
			ScriptRef scriptRef
				: scriptRefs ()
		) {

			formatWriter.writeLineFormat (
				"<script",
				" type=\"%h\"",
				scriptRef.getType (),
				" src=\"%h\"",
				scriptRef.getUrl (
					requestContext),
				"></script>");

		}

	}

	protected
	void renderHtmlLinks () {

		Set<? extends HtmlLink> links =
			htmlLinks ();

		if (links != null) {

			for (
				HtmlLink link
					: htmlLinks ()
			) {

				formatWriter.writeLineFormat (
					"%s",
					link.render (
						requestContext));

			}

		}

	}

	protected
	void renderHtmlHeadContents (
			@NonNull TaskLogger parentTaskLogger) {

		renderHtmlTitle ();

		renderHtmlScriptRefs ();

		renderHtmlLinks ();

		goMeta ();

	}

	protected
	void renderHtmlHead (
			@NonNull TaskLogger parentTaskLogger) {

		formatWriter.writeLineFormat (
			"<head>");

		formatWriter.increaseIndent ();

		renderHtmlHeadContents (
				parentTaskLogger);

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"</head>");

	}

	protected
	void renderHtmlBodyContents (
			@NonNull TaskLogger taskLogger) {

	}

	protected
	void renderHtmlBody (
			@NonNull TaskLogger taskLogger) {

		formatWriter.writeLineFormat (
			"<body>");

		formatWriter.increaseIndent ();

		renderHtmlBodyContents (
			taskLogger);

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"</body>");

	}

	@Override
	protected
	void render (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"render");

		renderHtmlDoctype ();

		formatWriter.writeLineFormat (
			"<html>");

		renderHtmlHead (
			taskLogger);

		renderHtmlBody (
			taskLogger);

		formatWriter.writeLineFormat (
			"</html>");

		renderDebugInformation (
			taskLogger);

	}

	private
	void renderDebugInformation (
			@NonNull TaskLogger parentTaskLogger) {

		if (
			! privChecker.canSimple (
				GlobalId.root,
				"debug")
		) {
			return;
		}

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"renderDebugInformation");

		formatWriter.writeLineFormatIncreaseIndent (
			"<!--");

		formatWriter.writeNewline ();

		if (
			optionalIsPresent (
				requestContext.consoleContext ())
		) {

			formatWriter.writeLineFormatIncreaseIndent (
				"Context data");

			formatWriter.writeNewline ();

			formatWriter.writeLineFormat (
				"Name: %s",
				requestContext.consoleContextRequired ().name ());

			formatWriter.writeLineFormat (
				"Type name: %s",
				requestContext.consoleContextRequired ().typeName ());

			formatWriter.writeLineFormat (
				"Path prefix: %s",
				requestContext.consoleContextRequired ().pathPrefix ());

			formatWriter.writeLineFormat (
				"Global: %s",
				booleanToYesNo (
					requestContext.consoleContextRequired ().global ()));

			if (
				isNotNull (
					requestContext.consoleContextRequired ().parentContextName ())
			) {

				formatWriter.writeLineFormat (
					"Parent context name: %s",
					requestContext.consoleContextRequired ().parentContextName ());

				formatWriter.writeLineFormat (
					"Parent context tab name: %s",
					requestContext.consoleContextRequired ().parentContextTabName ());

			}

			formatWriter.writeLineFormat ();

			if (
				optionalIsPresent (
					requestContext.foreignContextPath ())
			) {

				formatWriter.writeLineFormat (
					"Foreign context path: %s",
					requestContext.foreignContextPathRequired ());

			}

			if (
				optionalIsPresent (
					requestContext.changedContextPath ())
			) {

				formatWriter.writeLineFormat (
					"Changed context path: %s",
					requestContext.changedContextPathRequired ());

			}

			formatWriter.decreaseIndent ();

			formatWriter.writeNewline ();

		}

		if (
			optionalIsPresent (
				requestContext.consoleContextStuff ())
		) {

			formatWriter.writeLineFormatIncreaseIndent (
				"Context attributes");

			formatWriter.writeNewline ();

			for (
				Map.Entry <String, Object> attributeEntry
					: requestContext.consoleContextStuffRequired ()
						.attributes ()
						.entrySet ()
			) {

				formatWriter.writeLineFormat (
					"%s: %s",
					attributeEntry.getKey (),
					attributeEntry.getValue ().toString ());

			}

			formatWriter.decreaseIndent ();

			formatWriter.writeNewline ();

			formatWriter.writeLineFormatIncreaseIndent (
				"Context privs");

			formatWriter.writeNewline ();

			for (
				String priv
					: listSorted (
						requestContext.consoleContextStuffRequired ().privs ())
			) {

				formatWriter.writeLineFormat (
					"%s",
					priv);

			}

			formatWriter.decreaseIndent ();

			formatWriter.writeNewline ();

		}

		formatWriter.writeLineFormatIncreaseIndent (
			"Task log");

		formatWriter.writeNewline ();

		writeTaskLog (
			taskLogger.findRoot ());

		formatWriter.decreaseIndent ();

		formatWriter.writeNewline ();

		formatWriter.writeLineFormatDecreaseIndent (
			"-->");

	}

	private
	void writeTaskLog (
			@NonNull TaskLogEvent taskLogEvent) {

		formatWriter.writeLineFormatIncreaseIndent (
			"%s %s",
			enumName (
				taskLogEvent.eventSeverity ()),
			taskLogEvent.eventText ());

		taskLogEvent.eventChildren ().forEach (
			childEvent ->
				writeTaskLog (
					childEvent));

		formatWriter.decreaseIndent ();

	}

}
