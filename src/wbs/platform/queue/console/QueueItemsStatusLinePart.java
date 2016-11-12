package wbs.platform.queue.console;

import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.web.utils.HtmlAttributeUtils.htmlAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlIdAttribute;
import static wbs.web.utils.HtmlStyleUtils.htmlStyleBlockClose;
import static wbs.web.utils.HtmlStyleUtils.htmlStyleBlockOpen;
import static wbs.web.utils.HtmlStyleUtils.htmlStyleRuleClose;
import static wbs.web.utils.HtmlStyleUtils.htmlStyleRuleEntryWrite;
import static wbs.web.utils.HtmlStyleUtils.htmlStyleRuleOpen;
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
	void renderHtmlHeadContent (
			@NonNull TaskLogger parentTaskLogger) {

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
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

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
