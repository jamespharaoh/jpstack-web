package wbs.smsapps.broadcast.daemon;

import static wbs.utils.string.StringUtils.stringFormat;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;

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
		GenericScheduleDaemon <
			BroadcastConfigRec,
			BroadcastRec,
			BroadcastNumberRec
		> {

	// singleton dependencies

	@SingletonDependency
	BroadcastSendHelper broadcastSendHelper;

	// details

	@Override
	protected
	String friendlyName () {
		return "Broadcast schedule";
	}

	@Override
	protected
	String backgroundProcessName () {

		return stringFormat (
			"%s.schedule",
			broadcastSendHelper.parentTypeName ());

	}

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

}
