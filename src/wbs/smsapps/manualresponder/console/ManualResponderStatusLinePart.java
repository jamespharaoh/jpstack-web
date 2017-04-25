package wbs.smsapps.manualresponder.console;

import static wbs.web.utils.HtmlAttributeUtils.htmlIdAttribute;
import static wbs.web.utils.HtmlStyleUtils.htmlStyleRuleEntry;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import lombok.NonNull;

import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.html.ScriptRef;
import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("manualResponderStatusLinePart")
public
class ManualResponderStatusLinePart
	extends AbstractPagePart {

	@Override
	public
	Set <ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef> of (

			ConsoleApplicationScriptRef.javascript (
				"/js/manual-responder-status.js")

		);

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		htmlTableRowOpen (
			htmlIdAttribute (
				"manual-responder-row"),
			htmlStyleRuleEntry (
				"display",
				"none"));

		htmlTableCellWrite (
			"—",
			htmlIdAttribute (
				"manual-responder-cell"));

		htmlTableRowClose ();

	}

}
