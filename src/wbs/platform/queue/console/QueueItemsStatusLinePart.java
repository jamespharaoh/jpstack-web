package wbs.platform.queue.console;

import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.web.utils.HtmlAttributeUtils.htmlAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlIdAttribute;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import lombok.NonNull;

import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.html.HtmlLink;
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
				"/js/queue-items-status.js")

		);

	}

	@Override
	public
	Set <HtmlLink> links () {

		return ImmutableSet.<HtmlLink> of (

			HtmlLink.applicationCssStyle (
				"/style/queue-items-status.css")

		);

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
