package wbs.smsapps.broadcast.daemon;

import javax.inject.Inject;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.platform.send.GenericScheduleDaemon;
import wbs.platform.send.GenericSendHelper;
import wbs.smsapps.broadcast.logic.BroadcastSendHelper;
import wbs.smsapps.broadcast.model.BroadcastConfigRec;
import wbs.smsapps.broadcast.model.BroadcastNumberRec;
import wbs.smsapps.broadcast.model.BroadcastRec;

@SingletonComponent ("broadcastScheduleDaemon")
public
class BroadcastScheduleDaemon
	extends
		GenericScheduleDaemon<
			BroadcastConfigRec,
			BroadcastRec,
			BroadcastNumberRec
		> {

	// dependencies

	@Inject
	BroadcastSendHelper broadcastSendHelper;

	// implementation

	@Override
	protected
	GenericSendHelper<
		BroadcastConfigRec,
		BroadcastRec,
		BroadcastNumberRec
	> helper () {

		return broadcastSendHelper;

	}

}
