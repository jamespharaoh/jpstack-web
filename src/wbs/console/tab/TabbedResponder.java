package wbs.console.tab;

import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.string.StringUtils.joinWithoutSeparator;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.web.HtmlAttributeUtils.htmlAttribute;
import static wbs.utils.web.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.utils.web.HtmlAttributeUtils.htmlStyleAttribute;
import static wbs.utils.web.HtmlBlockUtils.htmlDivWrite;
import static wbs.utils.web.HtmlBlockUtils.htmlHeadingOneWrite;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphWrite;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphWriteHtml;
import static wbs.utils.web.HtmlScriptUtils.htmlScriptBlockClose;
import static wbs.utils.web.HtmlScriptUtils.htmlScriptBlockOpen;
import static wbs.utils.web.HtmlStyleUtils.htmlStyleRuleEntry;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellOpen;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpen;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowOpen;
import static wbs.utils.web.HtmlUtils.htmlLinkWrite;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.log4j.Logger;

import wbs.console.html.HtmlLink;
import wbs.console.html.ScriptRef;
import wbs.console.misc.ConsoleUserHelper;
import wbs.console.part.PagePart;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.HtmlResponder;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.ExceptionUtils;
import wbs.framework.exception.GenericExceptionResolution;

@Accessors (fluent = true)
@PrototypeComponent ("tabbedResponder")
public
class TabbedResponder
	extends HtmlResponder {

	// singleton dependencies

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	ExceptionLogger exceptionLogger;

	@SingletonDependency
	ExceptionUtils exceptionLogic;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
	ConsoleUserHelper consoleUserHelper;

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
	void prepare () {

		super.prepare ();

		TabContext tabContext =
			requestContext.tabContext ();

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

				pagePart.prepare ();

			} catch (RuntimeException exception) {

				String path =
					joinWithoutSeparator (
						requestContext.servletPath (),
						requestContext.pathInfo () != null
							? requestContext.pathInfo ()
							: "");

				// log the exception

				Logger logger =
					Logger.getLogger (
						getClass ());

				logger.warn (
					stringFormat (
						"Exception while reponding to: %s",
						path),
					exception);

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
	void renderHtmlHeadContents () {

		super.renderHtmlHeadContents ();

		pagePart.renderHtmlHeadContent ();

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
	void renderHtmlBodyContents () {

		htmlHeadingOneWrite (
			title);

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

		requestContext.flushNotices (
			formatWriter);

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

			pagePart.renderHtmlBodyContent ();

		}

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
