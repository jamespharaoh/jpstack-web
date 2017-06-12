package wbs.apn.chat.contact.console;

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

import wbs.web.responder.WebResponder;

@PrototypeComponent ("chatMonitorInboxQueueConsolePlugin")
public
class ChatMonitorInboxQueueConsolePlugin
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
	@NamedDependency ("chatMonitorInboxFormResponder")
	Provider <WebResponder> formResponderProvider;

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
	WebResponder makeResponder (
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
					"chatMonitorInbox",
					true);

			consoleManager.changeContext (
				taskLogger,
				targetContext,
				"/" + queueItem.getRefObjectId ());

			return formResponderProvider.get ();

		}

	}

}
