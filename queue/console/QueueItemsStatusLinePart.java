package wbs.platform.queue.console;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Set;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.context.ConsoleApplicationScriptRef;
import wbs.platform.console.html.ScriptRef;
import wbs.platform.console.part.AbstractPagePart;

import com.google.common.collect.ImmutableSet;

@PrototypeComponent ("queueItemsStatusLinePart")
public
class QueueItemsStatusLinePart
	extends AbstractPagePart {

	@Override
	public
	Set<ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef>of (

			ConsoleApplicationScriptRef.javascript (
				"/js/queue-status.js")

		);

	}

	@Override
	public
	void renderHtmlHeadContent () {

		printFormat (
			"<style type=\"text/css\">\n",
			"#queueRow { display: none; cursor: pointer; }\n",
			"</style>\n");

	}

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<tr",
			" id=\"queue-row\"",
			" onmouseover=\"this.className='hover';\"",
			" onmouseout=\"this.className='';\"",
			" onclick=\"%h\"",
			stringFormat (
				"top.frames ['inbox'].location = '%j';",
				requestContext.resolveApplicationUrl (
					"/queues/queue.home")),
			"><td",
			" id=\"queue-cell\"",
			">-</td>\n",
			"</tr>\n");

	}

}
