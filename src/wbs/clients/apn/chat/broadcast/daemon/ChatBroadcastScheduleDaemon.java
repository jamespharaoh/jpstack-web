package wbs.clients.apn.chat.broadcast.daemon;

import javax.inject.Inject;

import lombok.extern.log4j.Log4j;

import org.apache.log4j.Logger;

import wbs.clients.apn.chat.broadcast.logic.ChatBroadcastSendHelper;
import wbs.clients.apn.chat.broadcast.model.ChatBroadcastNumberRec;
import wbs.clients.apn.chat.broadcast.model.ChatBroadcastRec;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.platform.send.GenericScheduleDaemon;
import wbs.platform.send.GenericSendHelper;

@SingletonComponent ("chatBroadcastScheduleDaemon")
@Log4j
public
class ChatBroadcastScheduleDaemon
	extends
		GenericScheduleDaemon<
			ChatRec,
			ChatBroadcastRec,
			ChatBroadcastNumberRec
		> {

	// dependencies

	@Inject
	ChatBroadcastSendHelper chatBroadcastSendHelper;

	// implementation

	@Override
	protected
	GenericSendHelper<
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
