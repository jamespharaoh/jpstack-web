package wbs.platform.queue.console;

import wbs.console.helper.EnumConsoleHelper;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.platform.queue.model.QueueItemState;

@SingletonComponent ("queueItemStateConsoleHelper")
public
class QueueItemStateConsoleHelper
	extends EnumConsoleHelper<QueueItemState> {

	{

		enumClass (QueueItemState.class);

		add (QueueItemState.waiting, "waiting");
		add (QueueItemState.pending, "pending");
		add (QueueItemState.claimed, "claimed");
		add (QueueItemState.cancelled, "cancelled");
		add (QueueItemState.processed, "processed");

	}

}
