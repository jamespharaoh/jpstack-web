package wbs.smsapps.broadcast.daemon;

import lombok.extern.log4j.Log4j;

import org.apache.log4j.Logger;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.platform.send.GenericSendDaemon;
import wbs.platform.send.GenericSendHelper;
import wbs.smsapps.broadcast.logic.BroadcastSendHelper;
import wbs.smsapps.broadcast.model.BroadcastConfigRec;
import wbs.smsapps.broadcast.model.BroadcastNumberRec;
import wbs.smsapps.broadcast.model.BroadcastRec;

@Log4j
@SingletonComponent ("broadcastSendDaemon")
public
class BroadcastSendDaemon
	extends
		GenericSendDaemon <
			BroadcastConfigRec,
			BroadcastRec,
			BroadcastNumberRec
		> {

	// singleton dependencies

	@SingletonDependency
	BroadcastSendHelper broadcastSendHelper;

	// implementation

	@Override
	protected
	GenericSendHelper <
		BroadcastConfigRec,
		BroadcastRec,
		BroadcastNumberRec
	> helper () {

		return broadcastSendHelper;

	}

	@Override
	protected
	Logger log () {

		return log;

	}

}
