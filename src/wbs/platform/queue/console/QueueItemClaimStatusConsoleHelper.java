package wbs.platform.queue.console;

import wbs.console.helper.EnumConsoleHelper;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.platform.queue.model.QueueItemClaimStatus;

@SingletonComponent ("queueItemClaimStatusConsoleHelper")
public
class QueueItemClaimStatusConsoleHelper
	extends EnumConsoleHelper<QueueItemClaimStatus> {

	{

		enumClass (QueueItemClaimStatus.class);

		add (QueueItemClaimStatus.claimed, "claimed");
		add (QueueItemClaimStatus.unclaimed, "unclaimed");
		add (QueueItemClaimStatus.forcedUnclaim, "force unclaimed");
		add (QueueItemClaimStatus.processed, "processed");
		add (QueueItemClaimStatus.cancelled, "cancelled");

	}

}
