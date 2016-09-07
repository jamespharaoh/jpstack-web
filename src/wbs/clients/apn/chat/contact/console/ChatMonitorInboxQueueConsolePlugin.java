package wbs.clients.apn.chat.contact.console;

import wbs.console.context.ConsoleContext;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.module.ConsoleManager;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.annotations.SingletonDependency;
import wbs.framework.web.Responder;
import wbs.platform.queue.console.AbstractQueueConsolePlugin;
import wbs.platform.queue.model.QueueItemRec;

@PrototypeComponent ("chatMonitorInboxQueueConsolePlugin")
public
class ChatMonitorInboxQueueConsolePlugin
	extends AbstractQueueConsolePlugin {

	// singleton dependencies

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	ConsoleManager consoleManager;

	// details

	{

		queueTypeCode ("chat", "chat_gay_male");
		queueTypeCode ("chat", "chat_gay_female");
		queueTypeCode ("chat", "chat_bi_male");
		queueTypeCode ("chat", "chat_bi_female");
		queueTypeCode ("chat", "chat_straight_male");
		queueTypeCode ("chat", "chat_straight_female");
		queueTypeCode ("chat", "chat_unknown");

		queueTypeCode ("chat", "chat_gay_male_alarm");
		queueTypeCode ("chat", "chat_gay_female_alarm");
		queueTypeCode ("chat", "chat_bi_male_alarm");
		queueTypeCode ("chat", "chat_bi_female_alarm");
		queueTypeCode ("chat", "chat_straight_male_alarm");
		queueTypeCode ("chat", "chat_straight_female_alarm");
		queueTypeCode ("chat", "chat_unknown_alarm");

	}

	// implementation

	@Override
	public
	Responder makeResponder (
			QueueItemRec queueItem) {

		ConsoleContext targetContext =
			consoleManager.context (
				"chatMonitorInbox",
				true);

		consoleManager.changeContext (
			targetContext,
			"/" + queueItem.getRefObjectId ());

		return responder ("chatMonitorInboxFormResponder")
			.get ();

	}

}
