package wbs.console.tab;

import static wbs.utils.collection.CollectionUtils.listSorted;
import static wbs.utils.etc.LogicUtils.booleanToYesNo;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.string.StringUtils.joinWithoutSeparator;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.web.utils.HtmlAttributeUtils.htmlAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlStyleAttribute;
import static wbs.web.utils.HtmlBlockUtils.htmlDivWrite;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingOneWrite;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWrite;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWriteHtml;
import static wbs.web.utils.HtmlScriptUtils.htmlScriptBlockClose;
import static wbs.web.utils.HtmlScriptUtils.htmlScriptBlockOpen;
import static wbs.web.utils.HtmlStyleUtils.htmlStyleRuleEntry;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;
import static wbs.web.utils.HtmlUtils.htmlLinkWrite;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.html.HtmlLink;
import wbs.console.html.ScriptRef;
import wbs.console.misc.ConsoleUserHelper;
import wbs.console.part.PagePart;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.HtmlResponder;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.ExceptionUtils;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
@PrototypeComponent ("tabbedResponder")
public
class TabbedResponder
	extends HtmlResponder {

	// singleton dependencies

	@SingletonDependency
	ConsoleUserHelper consoleUserHelper;

	@SingletonDependency
	ExceptionLogger exceptionLogger;

	@SingletonDependency
	ExceptionUtils exceptionLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// properties

	@Getter @Setter
	Tab tab;

	@Getter @Setter
	String title;

	@Getter @Setter
	PagePart pagePart;

	// state

	Throwable pagePartThrew;

	List<MyLayer> myLayers =
		new ArrayList<MyLayer> ();

	// details

	@Override
	protected
	Set <HtmlLink> htmlLinks () {

		return ImmutableSet.<HtmlLink> builder ()

			.addAll (
				super.htmlLinks ())

			.addAll (
				pagePart.links ())

			.build ();

	}

	@Override
	protected
	Set <ScriptRef> scriptRefs () {

		return pagePart.scriptRefs ();

	}

	@Override
	protected
	String getTitle () {

		return title;

	}

	// implementation

	@Override
	protected
	void setup ()
		throws IOException {

		super.setup ();

		if (pagePart != null) {

			pagePart.setup (
				Collections.emptyMap ());

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

		TabContext tabContext =
			requestContext.tabContextRequired ();

		MyLayer myLayer1 = null;

		for (
			TabContext.Layer tabContextLayer
				: tabContext.getLayers ()
		) {

			myLayers.add (
				myLayer1 =
					new MyLayer ()

				.title (
					tabContextLayer.title ())

				.tabList (
					tabContextLayer.tabList ())

				.tab (
					tabContextLayer.tab ())

			);

		}

		if (myLayer1 == null) {
			throw new RuntimeException ();
		}

		myLayer1.tab (tab);

		if (pagePart != null) {

			try {

				pagePart.prepare (
					taskLogger);

			} catch (RuntimeException exception) {

				String path =
					joinWithoutSeparator (
						requestContext.servletPath (),
						requestContext.pathInfo () != null
							? requestContext.pathInfo ()
							: "");

				// log the exception

				taskLogger.warningFormatException (
					exception,
					"Exception while reponding to: %s",
					path);

				// record the exception

				exceptionLogger.logThrowable (
					"console",
					path,
					exception,
					consoleUserHelper.loggedInUserId (),
					GenericExceptionResolution.ignoreWithUserWarning);

				// and remember we had a problem

				pagePartThrew =
					exception;

				requestContext.addError (
					"Internal error");

			}

		}

	}

	@Override
	protected
	void renderHtmlHeadContents (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"renderHtmlHeadContents");

		super.renderHtmlHeadContents (
			taskLogger);

		if (

			isNotNull (
				pagePartThrew)

			&& isNotNull (
				pagePart)

		) {

			pagePart.renderHtmlHeadContent (
				taskLogger);

		}

		htmlScriptBlockOpen ();

		formatWriter.writeLineFormatIncreaseIndent (
			"function toggleHead (elem) {");

		formatWriter.writeLineFormatIncreaseIndent (
			"while (elem.nodeName.toLowerCase () != 'table') {");

		formatWriter.writeLineFormat (
			"elem = elem.parentNode;");

		formatWriter.writeLineFormatDecreaseIndent (
			"}");

		formatWriter.writeLineFormatIncreaseIndent (
			"if (elem.className == 'head-1-big') {");

		formatWriter.writeLineFormat (
			"elem.className = 'head-1-small';");

		formatWriter.writeLineFormatDecreaseIncreaseIndent (
			"} else if (elem.className == 'head-1-small') {");

		formatWriter.writeLineFormat (
			"elem.className = 'head-1-big';");

		formatWriter.writeLineFormatDecreaseIndent (
			"}");

		formatWriter.writeLineFormatDecreaseIndent (
			"}");

		htmlScriptBlockClose ();

	}

	protected
	void goTab () {
	}

	@Override
	protected
	void renderHtmlBodyContents (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"renderHtmlBodyContents");

		htmlHeadingOneWrite (
			title);

		renderTabs (
			taskLogger);

		requestContext.flushNotices (
			formatWriter);

		renderPagePart (
			taskLogger);

		renderDebugInformation (
			taskLogger);

	}

	// private implementation

	private
	void renderTabs (
			@NonNull TaskLogger parentTaskLogger) {

		for (
			MyLayer myLayer
				: myLayers
		) {

			htmlTableOpen (
				formatWriter,
				htmlClassAttribute (
					"head-1-big"));

			htmlTableRowOpen (
				formatWriter);

			htmlTableCellWrite (
				formatWriter,
				myLayer.title,
				htmlClassAttribute (
					"h"),
				htmlAttribute (
					"onclick",
					"toggleHead (this)"));

			htmlTableCellOpen (
				formatWriter,
				htmlClassAttribute (
					"l"));

			for (
				TabRef tabRef
					: myLayer.tabList.getTabRefs ()
			) {

				if (! tabRef.getTab ().isAvailable ())
					continue;

				if (tabRef.getTab () == myLayer.tab) {

					htmlLinkWrite (
						formatWriter,
						tabRef.getTab ().getUrl (),
						tabRef.getLabel (),
						htmlClassAttribute (
							"selected"));

				} else {

					htmlLinkWrite (
						formatWriter,
						tabRef.getTab ().getUrl (),
						tabRef.getLabel ());

				}

			}

			htmlTableCellClose (
				formatWriter);

			htmlTableRowClose (
				formatWriter);

			htmlTableClose (
				formatWriter);

		}

		htmlDivWrite (
			formatWriter,
			"",
			htmlStyleAttribute (
				htmlStyleRuleEntry (
					"clear",
					"both"),
				htmlStyleRuleEntry (
					"border-top",
					"1px solid white"),
				htmlStyleRuleEntry (
					"margin-bottom",
					"1ex")));

	}

	private
	void renderPagePart (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"renderPagePart");

		if (
			isNotNull (
				pagePartThrew)
		) {

			htmlParagraphWrite (
				"Unable to show page contents.");

			if (
				privChecker.canRecursive (
					GlobalId.root,
					"debug")
			) {

				htmlParagraphWriteHtml (
					formatWriter,
					stringFormat (
						"<pre>%h</pre>",
						exceptionLogic.throwableDump (
							pagePartThrew)));

			}

		} else if (
			isNotNull (
				pagePart)
		) {

			pagePart.renderHtmlBodyContent (
				taskLogger);

		}

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

		formatWriter.writeLineFormatIncreaseIndent (
			"<!--");

		formatWriter.writeNewline ();

		formatWriter.writeLineFormatIncreaseIndent (
			"Context data");

		formatWriter.writeNewline ();

		formatWriter.writeLineFormat (
			"Name: %s",
			requestContext.consoleContext ().name ());

		formatWriter.writeLineFormat (
			"Type name: %s",
			requestContext.consoleContext ().typeName ());

		formatWriter.writeLineFormat (
			"Path prefix: %s",
			requestContext.consoleContext ().pathPrefix ());

		formatWriter.writeLineFormat (
			"Global: %s",
			booleanToYesNo (
				requestContext.consoleContext ().global ()));

		if (
			isNotNull (
				requestContext.consoleContext ().parentContextName ())
		) {

			formatWriter.writeLineFormat (
				"Parent context name: %s",
				requestContext.consoleContext ().parentContextName ());

			formatWriter.writeLineFormat (
				"Parent context tab name: %s",
				requestContext.consoleContext ().parentContextTabName ());

		}

		formatWriter.writeLineFormat ();

		formatWriter.writeLineFormat (
			"Foreign context path: %s",
			requestContext.foreignContextPath ());

		if (
			isNotNull (
				requestContext.changedContextPath ())
		) {

			formatWriter.writeLineFormat (
				"Changed context path: %s",
				requestContext.changedContextPath ());

		}

		formatWriter.decreaseIndent ();

		formatWriter.writeNewline ();

		formatWriter.writeLineFormatIncreaseIndent (
			"Context attributes");

		formatWriter.writeNewline ();

		for (
			Map.Entry <String, Object> attributeEntry
				: requestContext.contextStuff ().attributes ().entrySet ()
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
					requestContext.contextStuff ().privs ())
		) {

			formatWriter.writeLineFormat (
				"%s",
				priv);

		}

		formatWriter.decreaseIndent ();

		formatWriter.writeNewline ();

		formatWriter.writeLineFormatDecreaseIndent (
			"-->");

	}

	@Accessors (fluent = true)
	@Data
	private static
	class MyLayer {
		String title;
		TabList tabList;
		Tab tab;
	}

}
