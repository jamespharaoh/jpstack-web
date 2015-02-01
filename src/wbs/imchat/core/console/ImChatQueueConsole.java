package wbs.imchat.core.console;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.web.Responder;
import wbs.imchat.core.model.ImChatRec;
import wbs.platform.console.context.ConsoleContext;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.console.module.ConsoleManager;
import wbs.platform.queue.console.AbstractQueueConsolePlugin;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.queue.model.QueueRec;

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
				"imChatMessage",
				true);

		consoleManager.changeContext (
			targetContext,
			"/" + queueItem.getRefObjectId ());

		return responder (
			"imChatMessageReplyResponder"
		).get ();

	}

	@Override
	public
	long preferredUserDelay (
			QueueRec queue) {

		ImChatRec imChat =
			(ImChatRec) (Object)
			objectManager.getParent (
				queue);

		return imChat.getPreferredQueueTime () * 1000L;

	}

}
