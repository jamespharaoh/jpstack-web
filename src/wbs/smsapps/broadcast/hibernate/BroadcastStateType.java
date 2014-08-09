package wbs.smsapps.broadcast.hibernate;

import wbs.framework.hibernate.EnumUserType;
import wbs.smsapps.broadcast.model.BroadcastState;

public
class BroadcastStateType
	extends EnumUserType<String,BroadcastState> {

	{

		sqlType (1111);
		enumClass (BroadcastState.class);

		add ("unsent", BroadcastState.unsent);
		add ("scheduled", BroadcastState.scheduled);
		add ("sending", BroadcastState.sending);
		add ("sent", BroadcastState.sent);
		add ("partially-sent", BroadcastState.partiallySent);
		add ("cancelled", BroadcastState.cancelled);

	}

}
