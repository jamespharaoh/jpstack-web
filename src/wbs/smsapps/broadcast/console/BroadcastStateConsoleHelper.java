package wbs.smsapps.broadcast.console;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.platform.console.helper.EnumConsoleHelper;
import wbs.smsapps.broadcast.model.BroadcastState;

@SingletonComponent ("broadcastStateConsoleHelper")
public
class BroadcastStateConsoleHelper
	extends EnumConsoleHelper<BroadcastState> {

	{

		enumClass (BroadcastState.class);

		add (BroadcastState.unsent, "unsent");
		add (BroadcastState.scheduled, "scheduled");
		add (BroadcastState.sending, "sending");
		add (BroadcastState.sent, "sent");
		add (BroadcastState.partiallySent, "partially sent");
		add (BroadcastState.cancelled, "cancelled");

	}

}
