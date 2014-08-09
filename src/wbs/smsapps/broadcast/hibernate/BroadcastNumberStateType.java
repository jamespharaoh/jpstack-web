package wbs.smsapps.broadcast.hibernate;

import wbs.framework.hibernate.EnumUserType;
import wbs.smsapps.broadcast.model.BroadcastNumberState;

public
class BroadcastNumberStateType
	extends EnumUserType<String,BroadcastNumberState> {

	{

		sqlType (1111);
		enumClass (BroadcastNumberState.class);

		add ("accepted", BroadcastNumberState.accepted);
		add ("rejected", BroadcastNumberState.rejected);
		add ("sent", BroadcastNumberState.sent);
		add ("removed", BroadcastNumberState.removed);

	}

}
