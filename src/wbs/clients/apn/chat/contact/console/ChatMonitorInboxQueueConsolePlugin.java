package wbs.clients.apn.chat.contact.console;

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

@PrototypeComponent ("chatMonitorInboxQueueConsolePlugin")
public
class ChatMonitorInboxQueueConsolePlugin
	extends AbstractQueueConsolePlugin {

	// dependencies

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
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

	@Override
	public
	long preferredUserDelay (
			QueueRec queue) {

		ChatRec chat =
			(ChatRec) (Object)
			objectManager.getParent (
				queue);

		return chat.getChatQueuePreferredTime () * 1000L;

	}

}
