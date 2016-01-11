package wbs.applications.imchat.console;

import javax.inject.Inject;

import wbs.console.context.ConsoleContext;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.module.ConsoleManager;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.web.Responder;
import wbs.platform.queue.console.AbstractQueueConsolePlugin;
import wbs.platform.queue.model.QueueItemRec;

@PrototypeComponent ("imChatQueueConsole")
public
class ImChatQueueConsole
	extends AbstractQueueConsolePlugin {

	// dependencies

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	ConsoleManager consoleManager;

	// details

	{
		queueTypeCode ("im_chat", "reply");
	}

	// implementation

	@Override
	public
	Responder makeResponder (
			QueueItemRec queueItem) {

		ConsoleContext targetContext =
			consoleManager.context (
				"imChat.pending",
				true);

		consoleManager.changeContext (
			targetContext,
			"/" + queueItem.getRefObjectId ());

		return responder (
			"imChatPendingFormResponder"
		).get ();

	}

}
