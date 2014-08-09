package wbs.smsapps.broadcast.console;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.platform.console.helper.EnumConsoleHelper;
import wbs.smsapps.broadcast.model.BroadcastNumberState;

@SingletonComponent ("broadcastNumberStateConsoleHelper")
public
class BroadcastNumberStateConsoleHelper
	extends EnumConsoleHelper<BroadcastNumberState> {

	{

		enumClass (BroadcastNumberState.class);

		add (BroadcastNumberState.accepted, "accepted");
		add (BroadcastNumberState.rejected, "rejected");
		add (BroadcastNumberState.sent, "sent");
		add (BroadcastNumberState.removed, "removed");

	}

}
