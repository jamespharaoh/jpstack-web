package wbs.apn.chat.user.pending.console;

import lombok.NonNull;

import wbs.console.context.ConsoleContext;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.module.ConsoleManager;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;
import wbs.platform.queue.console.AbstractQueueConsolePlugin;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.queue.model.QueueSubjectRec;
import wbs.web.responder.Responder;

@PrototypeComponent ("chatUserQueueConsolePlugin")
public
class ChatUserQueueConsolePlugin
	extends AbstractQueueConsolePlugin {

	// singleton dependencies

	@SingletonDependency
	ConsoleManager consoleManager;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	// details

	{
		queueTypeCode ("chat",  "user");
	}

	// implementation

	@Override
	public
	Responder makeResponder (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull QueueItemRec queueItem) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"makeResponder");

		QueueSubjectRec queueSubject =
			queueItem.getQueueSubject ();

		ConsoleContext targetContext =
			consoleManager.context (
				"chatUser.pending",
				true);

		consoleManager.changeContext (
			taskLogger,
			targetContext,
			"/" + queueSubject.getObjectId ());

		return responder (
			"chatUserPendingFormResponder"
		).get ();

	}

}
