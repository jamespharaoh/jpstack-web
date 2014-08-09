package wbs.smsapps.manualresponder.console;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.web.Responder;
import wbs.platform.console.context.ConsoleContext;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.console.module.ConsoleManager;
import wbs.platform.queue.console.AbstractQueueConsolePlugin;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.queue.model.QueueRec;
import wbs.smsapps.manualresponder.model.ManualResponderRec;

@PrototypeComponent ("manualResponderQueueConsolePlugin")
public
class ManualResponderQueueConsolePlugin
	extends AbstractQueueConsolePlugin {

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	ConsoleManager consoleManager;

	{
		queueTypeCode ("manual_responder", "default");
	}

	@Override
	public
	Responder makeResponder (
			QueueItemRec queueItem) {

		ConsoleContext targetContext =
			consoleManager.context (
				"manualResponderRequest.pending",
				true);

		consoleManager.changeContext (
			targetContext,
			"/" + queueItem.getRefObjectId ());

		return responder (
			"manualResponderRequestPendingFormResponder"
		).get ();

	}

	@Override
	public
	long preferredUserDelay (
			QueueRec queue) {

		ManualResponderRec manualResponder =
			(ManualResponderRec) (Object)
			objectManager.getParent (
				queue);

		return manualResponder.getPreferredQueueTime () * 1000L;

	}

}
