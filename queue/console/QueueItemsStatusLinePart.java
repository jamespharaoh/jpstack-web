package wbs.platform.queue.console;

import static wbs.framework.utils.etc.Misc.stringFormat;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.part.AbstractPagePart;

@PrototypeComponent ("queueItemsStatusLinePart")
public
class QueueItemsStatusLinePart
	extends AbstractPagePart {

	@Override
	public
	void goHeadStuff () {

		printFormat (
			"<style type=\"text/css\">\n",
			"#queueRow { display: none; cursor: pointer; }\n",
			"</style>\n");

		printFormat (
			"<script type=\"text/javascript\">\n",
			"function updateQueueItems (numQueue) {\n",
			"  var queueCell = document.getElementById ('queueCell');\n",
			"  var queueRow = document.getElementById ('queueRow');\n",
			"  if (numQueue > 0) queueCell.firstChild.data = '' + numQueue + ' items queueing';\n",
			"  showTableRow (queueRow, numQueue > 0);\n",
			"}\n",
			"</script>\n");

	}

	@Override
	public
	void goBodyStuff () {

		printFormat (
			"<tr id=\"queueRow\"" +
			" onmouseover=\"this.className='hover';\"" +
			" onmouseout=\"this.className='';\"" +
			" onclick=\"%h\"> <td id=\"queueCell\">-</td> </tr>\n",
			stringFormat (
				"top.frames ['inbox'].location = '%j';",
				requestContext.resolveApplicationUrl (
					"/queues/queue.home")));

	}

}
