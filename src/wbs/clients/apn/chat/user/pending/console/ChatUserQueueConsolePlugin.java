package wbs.clients.apn.chat.user.pending.console;

import wbs.console.context.ConsoleContext;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.module.ConsoleManager;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.web.Responder;
import wbs.platform.queue.console.AbstractQueueConsolePlugin;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.queue.model.QueueSubjectRec;

@PrototypeComponent ("chatUserQueueConsolePlugin")
public
class ChatUserQueueConsolePlugin
	extends AbstractQueueConsolePlugin {

	// singleton dependencies

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	ConsoleManager consoleManager;

	// details

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

}
