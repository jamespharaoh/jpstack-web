package wbs.smsapps.ticketer.daemon;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.object.ObjectManager;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.logic.SmsInboxLogic;
import wbs.sms.message.inbox.model.InboxAttemptRec;
import wbs.sms.message.inbox.model.InboxRec;
import wbs.sms.message.outbox.logic.SmsMessageSender;
import wbs.smsapps.ticketer.model.TicketerRec;
import wbs.smsapps.ticketer.model.TicketerTicketObjectHelper;
import wbs.utils.random.RandomLogic;

@Accessors (fluent = true)
@PrototypeComponent ("ticketerCommand")
public
class TicketerCommand
	implements CommandHandler {

	// singleton dependencies

	@SingletonDependency
	CommandObjectHelper commandHelper;

	@SingletonDependency
	Database database;

	@SingletonDependency
	SmsInboxLogic smsInboxLogic;

	@SingletonDependency
	MessageObjectHelper messageHelper;

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	RandomLogic randomLogic;

	@SingletonDependency
	ServiceObjectHelper serviceHelper;

	@SingletonDependency
	TicketerTicketObjectHelper ticketerTicketHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider <SmsMessageSender> messageSender;

	// properties

	@Getter @Setter
	InboxRec inbox;

	@Getter @Setter
	CommandRec command;

	@Getter @Setter
	Optional<Long> commandRef;

	@Getter @Setter
	String rest;

	// details

	@Override
	public
	String[] getCommandTypes () {

		return new String [] {
			"ticketer.ticketer"
		};

	}

	// implementation

	@Override
	public
	InboxAttemptRec handle () {

		TicketerRec ticketer =
			(TicketerRec) (Object)
			objectManager.getParent (
				command);

		// set inbound message stuff

		ServiceRec defaultService =
			serviceHelper.findByCodeRequired (
				ticketer,
				"default");

		// load message and stuff

		MessageRec messageIn =
			inbox.getMessage ();

		// work out ticket and message text

		String ticket =
			randomLogic.generateUppercase (8);

		String messageText =
			ticketer.getText ().replaceAll (
				"\\{ticket\\}",
				ticket);

		// send message

		MessageRec messageOut =
			messageSender.get ()

			.threadId (
				messageIn.getThreadId ())

			.number (
				messageIn.getNumber ())

			.messageString (
				messageText)

			.numFrom (
				ticketer.getNumber ())

			.route (
				ticketer.getRoute ())

			.service (
				defaultService)

			.send ();

		// create ticketer ticket entry

		ticketerTicketHelper.insert (
			ticketerTicketHelper.createInstance ()

			.setTicketer (
				ticketer)

			.setNumber (
				messageIn.getNumber ())

			.setTicket (
				ticket)

			.setMessage (
				messageOut)

		);

		// process inbox

		return smsInboxLogic.inboxProcessed (
			inbox,
			Optional.of (
				defaultService),
			Optional.<AffiliateRec>absent (),
			command);

	}

}
