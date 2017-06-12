package wbs.apn.chat.user.pending.console;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.console.context.ConsoleContext;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.module.ConsoleManager;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NamedDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.platform.queue.console.AbstractQueueConsolePlugin;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.queue.model.QueueSubjectRec;

import wbs.web.responder.WebResponder;

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

	// prototype dependencies

	@PrototypeDependency
	@NamedDependency ("chatUserPendingFormResponder")
	Provider <WebResponder> pendingFormResponderProvider;

	// details

	{
		queueTypeCode ("chat",  "user");
	}

	// implementation

	@Override
	public
	WebResponder makeResponder (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull QueueItemRec queueItem) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"makeResponder");

		) {

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

			return pendingFormResponderProvider.get ();

		}

	}

}
