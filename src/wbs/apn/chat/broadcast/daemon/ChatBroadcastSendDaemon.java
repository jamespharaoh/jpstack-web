package wbs.apn.chat.broadcast.daemon;

import lombok.extern.log4j.Log4j;

import org.apache.log4j.Logger;

import wbs.apn.chat.broadcast.logic.ChatBroadcastSendHelper;
import wbs.apn.chat.broadcast.model.ChatBroadcastNumberRec;
import wbs.apn.chat.broadcast.model.ChatBroadcastRec;
import wbs.apn.chat.core.model.ChatRec;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.platform.send.GenericSendDaemon;
import wbs.platform.send.GenericSendHelper;

@Log4j
@SingletonComponent ("chatBroadcastSendDaemon")
public
class ChatBroadcastSendDaemon
	extends
		GenericSendDaemon <
			ChatRec,
			ChatBroadcastRec,
			ChatBroadcastNumberRec
		> {

	// singleton dependencies

	@SingletonDependency
	ChatBroadcastSendHelper chatBroadcastSendHelper;

	// implementation

	@Override
	protected
	GenericSendHelper <
		ChatRec,
		ChatBroadcastRec,
		ChatBroadcastNumberRec
	> helper () {

		return chatBroadcastSendHelper;

	}

	@Override
	protected
	Logger log () {

		return log;

	}

}
