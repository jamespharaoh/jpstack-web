package wbs.clients.apn.chat.help.console;

import wbs.console.context.ConsoleContext;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.module.ConsoleManager;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.web.Responder;
import wbs.platform.queue.console.AbstractQueueConsolePlugin;
import wbs.platform.queue.model.QueueItemRec;

@PrototypeComponent ("chatHelpQueueConsolePlugin")
public
class ChatHelpQueueConsolePlugin
	extends AbstractQueueConsolePlugin {

	// singleton dependencies

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	ConsoleManager consoleManager;

	// details

	{
		queueTypeCode ("chat", "help");
	}

	@Override
	public
	Responder makeResponder (
			QueueItemRec queueItem) {

		ConsoleContext targetContext =
			consoleManager.context (
				"chatHelpLog.pending",
				true);

		consoleManager.changeContext (
			targetContext,
			"/" + queueItem.getRefObjectId ());

		return responder ("chatHelpLogPendingFormResponder")
			.get ();

	}

}
