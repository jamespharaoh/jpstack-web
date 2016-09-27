package wbs.platform.queue.console;

import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.web.HtmlAttributeUtils.htmlAttribute;
import static wbs.utils.web.HtmlAttributeUtils.htmlIdAttribute;
import static wbs.utils.web.HtmlStyleUtils.htmlStyleBlockClose;
import static wbs.utils.web.HtmlStyleUtils.htmlStyleBlockOpen;
import static wbs.utils.web.HtmlStyleUtils.htmlStyleRuleClose;
import static wbs.utils.web.HtmlStyleUtils.htmlStyleRuleEntryWrite;
import static wbs.utils.web.HtmlStyleUtils.htmlStyleRuleOpen;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowOpen;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.html.ScriptRef;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;

@PrototypeComponent ("queueItemsStatusLinePart")
public
class QueueItemsStatusLinePart
	extends AbstractPagePart {

	@Override
	public
	Set <ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef> of (

			ConsoleApplicationScriptRef.javascript (
				"/js/queue-status.js")

		);

	}

	@Override
	public
	void renderHtmlHeadContent () {

		htmlStyleBlockOpen ();

		htmlStyleRuleOpen (
			"#queueRow");

		htmlStyleRuleEntryWrite (
			"display",
			"none");

		htmlStyleRuleEntryWrite (
			"cursor",
			"pointer");

		htmlStyleRuleClose ();

		htmlStyleBlockClose ();

	}

	@Override
	public
	void renderHtmlBodyContent () {

		htmlTableRowOpen (

			htmlIdAttribute (
				"queue-row"),

			htmlAttribute (
				"onmouseover",
				"this.className='hover';"),

			htmlAttribute (
				"onmouseout",
				"this.className='';"),

			htmlAttribute (
				"onclick",
				stringFormat (
					"top.frames ['inbox'].location = '%j';",
					requestContext.resolveApplicationUrl (
						"/queues/queue.home")))
		);

		htmlTableCellWrite (
			"—",
			htmlIdAttribute (
				"queue-cell"));

		htmlTableRowClose ();

	}

}
