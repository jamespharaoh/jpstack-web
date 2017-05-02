package wbs.console.responder;

import static wbs.utils.collection.CollectionUtils.listSorted;
import static wbs.utils.etc.EnumUtils.enumName;
import static wbs.utils.etc.LogicUtils.booleanToYesNo;
import static wbs.utils.etc.Misc.doNothing;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import lombok.NonNull;

import wbs.console.html.HtmlLink;
import wbs.console.html.ScriptRef;
import wbs.console.priv.UserPrivChecker;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogEvent;
import wbs.framework.logging.TaskLogger;

import wbs.utils.time.TimeFormatter;

public abstract
class ConsoleHtmlResponder
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
	void setHtmlHeaders (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"setHtmlHeaders");

		) {

			super.setHtmlHeaders (
				transaction);

			requestContext.setHeader (
				"Content-Type",
				"text/html; charset=utf-8");

			requestContext.setHeader (
				"Cache-Control",
				"no-cache");

			requestContext.setHeader (
				"Expiry",
				timeFormatter.httpTimestampString (
					transaction.now ()));

		}

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
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlHeadContents");

		) {

			renderHtmlTitle ();

			renderHtmlScriptRefs ();

			renderHtmlLinks ();

			goMeta ();

		}

	}

	protected
	void renderHtmlHead (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlHead");

		) {

			formatWriter.writeLineFormatIncreaseIndent (
				"<head>");

			renderHtmlHeadContents (
				transaction);

			formatWriter.writeLineFormatDecreaseIndent (
				"</head>");

		}

	}

	protected
	void renderHtmlBodyContents (
			@NonNull Transaction parentTransaction) {

		doNothing ();

	}

	protected
	void renderHtmlBody (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBody");

		) {

			formatWriter.writeLineFormatIncreaseIndent (
				"<body>");

			renderHtmlBodyContents (
				transaction);

			formatWriter.writeLineFormatDecreaseIndent (
				"</body>");

		}

	}

	@Override
	protected
	void render (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"render");

		) {

			renderHtmlDoctype ();

			formatWriter.writeLineFormat (
				"<html>");

			renderHtmlHead (
				transaction);

			renderHtmlBody (
				transaction);

			formatWriter.writeLineFormat (
				"</html>");

			renderDebugInformation (
				transaction);

		}

	}

	private
	void renderDebugInformation (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"renderDebugInformation");

		) {

			if (
				! privChecker.canSimple (
					taskLogger,
					GlobalId.root,
					"debug")
			) {
				return;
			}

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
