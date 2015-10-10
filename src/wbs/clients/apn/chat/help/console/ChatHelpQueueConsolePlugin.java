package wbs.clients.apn.chat.help.console;

import javax.inject.Inject;

import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.console.context.ConsoleContext;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.module.ConsoleManager;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.web.Responder;
import wbs.platform.queue.console.AbstractQueueConsolePlugin;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.queue.model.QueueRec;

@PrototypeComponent ("chatHelpQueueConsolePlugin")
public
class ChatHelpQueueConsolePlugin
	extends AbstractQueueConsolePlugin {

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	ConsoleManager consoleManager;

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

	@Override
	public
	long preferredUserDelay (
			QueueRec queue) {

		ChatRec chat =
			(ChatRec) (Object)
			objectManager.getParent (
				queue);

		return chat.getHelpQueuePreferredTime () * 1000L;

	}

}
