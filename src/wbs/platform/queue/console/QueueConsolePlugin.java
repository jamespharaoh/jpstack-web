package wbs.platform.queue.console;

import java.util.List;

import wbs.framework.web.Responder;
import wbs.platform.queue.model.QueueItemRec;

public
interface QueueConsolePlugin {

	List<String> queueTypeCodes ();

	Responder makeResponder (
			QueueItemRec queueItem);

}
