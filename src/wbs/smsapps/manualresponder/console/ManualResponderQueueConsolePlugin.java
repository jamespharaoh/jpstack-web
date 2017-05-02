package wbs.smsapps.manualresponder.console;

import lombok.NonNull;

import wbs.console.context.ConsoleContext;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.module.ConsoleManager;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.platform.queue.console.AbstractQueueConsolePlugin;
import wbs.platform.queue.model.QueueItemRec;

import wbs.web.responder.Responder;

@PrototypeComponent ("manualResponderQueueConsolePlugin")
public
class ManualResponderQueueConsolePlugin
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
		queueTypeCode ("manual_responder", "default");
	}

	@Override
	public
	Responder makeResponder (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull QueueItemRec queueItem) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"makeResponder");

		) {

			ConsoleContext targetContext =
				consoleManager.context (
					"manualResponderRequest.pending",
					true);

			consoleManager.changeContext (
				taskLogger,
				targetContext,
				"/" + queueItem.getRefObjectId ());

			return responder (
				"manualResponderRequestPendingFormResponder"
			).get ();

		}

	}

}
