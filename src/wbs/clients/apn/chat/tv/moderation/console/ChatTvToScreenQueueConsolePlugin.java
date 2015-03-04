package wbs.clients.apn.chat.tv.moderation.console;

import javax.inject.Inject;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.web.Responder;
import wbs.platform.console.context.ConsoleContext;
import wbs.platform.console.module.ConsoleManager;
import wbs.platform.queue.console.AbstractQueueConsolePlugin;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.queue.model.QueueRec;
import wbs.platform.queue.model.QueueSubjectRec;

@SingletonComponent ("chatTvToScreenQueueConsolePlugin")
public
class ChatTvToScreenQueueConsolePlugin
	extends AbstractQueueConsolePlugin {

	@Inject ConsoleManager consoleManager;

	{
		queueTypeCode ("chat", "tv_to_screen");
	}

	@Override
	public
	Responder makeResponder (
			QueueItemRec queueItem) {

		QueueSubjectRec queueSubject =
			queueItem.getQueueSubject ();

		ConsoleContext targetContext =
			consoleManager.context (
				"chatTvModeration",
				true);

		consoleManager.changeContext (
			targetContext,
			"/" + queueSubject.getObjectId ());

		return responder ("chatTvModerationFormResponder")
			.get ();

	}

	@Override
	public
	long preferredUserDelay (
			QueueRec queue) {

		return 150 * 1000;

	}

}
