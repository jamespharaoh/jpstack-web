package wbs.clients.apn.chat.tv.core.daemon;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.sms.command.model.CommandRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.model.InboxAttemptRec;
import wbs.sms.message.inbox.model.InboxRec;

import com.google.common.base.Optional;

@Accessors (fluent = true)
@PrototypeComponent ("chatTvToScreenCommand")
public
class ChatTvToScreenCommand
	implements CommandHandler {

	// properties

	@Getter @Setter
	InboxRec inbox;

	@Getter @Setter
	CommandRec command;

	@Getter @Setter
	Optional<Integer> commandRef;

	@Getter @Setter
	String rest;

	// details

	@Override
	public
	String[] getCommandTypes () {

		return new String [] {
			"chat.tv_to_screen"
		};

	}

	// implementation

	@Override
	public
	InboxAttemptRec handle () {

		throw new RuntimeException ();

	}

}