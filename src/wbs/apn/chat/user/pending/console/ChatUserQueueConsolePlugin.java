package wbs.apn.chat.user.pending.console;

import javax.inject.Inject;

import wbs.apn.chat.core.model.ChatRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.web.Responder;
import wbs.platform.console.context.ConsoleContext;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.console.module.ConsoleManager;
import wbs.platform.queue.console.AbstractQueueConsolePlugin;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.queue.model.QueueRec;
import wbs.platform.queue.model.QueueSubjectRec;

@PrototypeComponent ("chatUserQueueConsolePlugin")
public
class ChatUserQueueConsolePlugin
	extends AbstractQueueConsolePlugin {

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	ConsoleManager consoleManager;

	{
		queueTypeCode ("chat",  "user");
	}

	// implementation

	@Override
	public
	Responder makeResponder (
			QueueItemRec queueItem) {

		QueueSubjectRec queueSubject =
			queueItem.getQueueSubject ();

		ConsoleContext targetContext =
			consoleManager.context (
				"chatUser.pending",
				true);

		consoleManager.changeContext (
			targetContext,
			"/" + queueSubject.getObjectId ());

		return responder (
			"chatUserPendingFormResponder"
		).get ();

	}

	@Override
	public
	long preferredUserDelay (
			QueueRec queue) {

		ChatRec chat =
			(ChatRec) (Object)
			objectManager.getParent (
				queue);

		return chat.getInfoQueuePreferredTime () * 1000L;

	}

}
