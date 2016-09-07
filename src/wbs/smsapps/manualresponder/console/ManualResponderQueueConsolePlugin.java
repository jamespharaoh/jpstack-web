package wbs.smsapps.manualresponder.console;

import javax.inject.Inject;

import wbs.console.context.ConsoleContext;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.module.ConsoleManager;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.web.Responder;
import wbs.platform.queue.console.AbstractQueueConsolePlugin;
import wbs.platform.queue.model.QueueItemRec;

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

}
