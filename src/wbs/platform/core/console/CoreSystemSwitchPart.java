package wbs.platform.core.console;

import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.web.utils.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWriteFormat;
import static wbs.web.utils.HtmlInputUtils.htmlRadio;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWriteHtml;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import lombok.NonNull;

import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("coreSystemSwitchPart")
public
class CoreSystemSwitchPart
	extends AbstractPagePart {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	Set <ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef> of (

			JqueryScriptRef.instance,

			ConsoleApplicationScriptRef.javascript (
				"/js/js-cookie-2.0.4.js"),

			ConsoleApplicationScriptRef.javascript (
				"/js/core-system-switch.js")

		);

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		@SuppressWarnings ("unused")
		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"renderHtmlBodyContent");

		htmlHeadingTwoWrite (
			"Switch deployment mode");

		htmlParagraphWriteFormat (
			"Use this control to select which version of the console you are ",
			"using. This allows you to switch to a newer or older version of ",
			"the console, in case of problems, and also to test new versions ",
			"which are not yet ready to be released.");

		htmlParagraphWriteFormat (
			"The default should be \"current\", and this is what you should ",
			"select if you are not sure, unless you have been told otherwise.");

		htmlTableOpenDetails ();

		htmlTableHeaderRowWrite (
			"",
			"Name",
			"Description");

		for (
			Map.Entry <String, String> modeEntry
				: modes.entrySet ()
		) {

			String modeName =
				modeEntry.getKey ();

			String modeDescription =
				modeEntry.getValue ();

			htmlTableRowOpen (
				htmlClassAttribute (
					"core-system-switch-row"));

			htmlTableCellWriteHtml (
				() -> htmlRadio (
					"mode",
					modeName,
					false),
				htmlClassAttribute (
					"core-system-switch-mode"));

			htmlTableCellWrite (
				modeName);

			htmlTableCellWrite (
				modeDescription);

			htmlTableRowClose ();

		}

		htmlTableClose ();

	}

	// data

	private final static
	Map <String, String> modes =
		ImmutableMap.<String, String> builder ()

		.put (
			"current",
			stringFormat (
				"Current version, this is the version you should normally ",
				"use, unless you are told otherwise or having problems caused ",
				"by new changes."))

		.put (
			"previous",
			stringFormat (
				"Previous version, use this if you have trouble with new ",
				"features which have been released to current."))

		.put (
			"next",
			stringFormat (
				"Next version, use this if you have been asked to test the ",
				"new version before it is made current."))

		.put (
			"test",
			stringFormat (
				"Test version for very new and/or incomplete features. Only ",
				"use this if you are asked specifically to."))

		.put (
			"dev",
			stringFormat (
				"Development version, which will regularly be very broken. ",
				"Only use this if you are asked specifically to."))

		.build ();

}
