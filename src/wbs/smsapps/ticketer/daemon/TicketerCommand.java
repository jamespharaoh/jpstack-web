package wbs.smsapps.ticketer.daemon;

import java.util.Random;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import lombok.NonNull;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.ObjectManager;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.daemon.ReceivedMessage;
import wbs.sms.message.inbox.logic.InboxLogic;
import wbs.sms.message.outbox.logic.MessageSender;
import wbs.smsapps.ticketer.model.TicketerRec;
import wbs.smsapps.ticketer.model.TicketerTicketObjectHelper;
import wbs.smsapps.ticketer.model.TicketerTicketRec;

@PrototypeComponent ("ticketerCommand")
public
class TicketerCommand
	implements CommandHandler {

	// dependencies

	@Inject
	CommandObjectHelper commandHelper;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	Database database;

	@Inject
	InboxLogic inboxLogic;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	ObjectManager objectManager;

	@Inject
	Random random;

	@Inject
	TicketerTicketObjectHelper ticketerTicketHelper;

	@Inject
	Provider<MessageSender> messageSender;

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
	void handle (
			int commandId,
			@NonNull ReceivedMessage receivedMessage) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		CommandRec command =
			commandHelper.find (
				commandId);

		TicketerRec ticketer =
			(TicketerRec) (Object)
			objectManager.getParent (
				command);

		// set inbound message stuff

		ServiceRec defaultService =
			serviceHelper.findByCode (
				ticketer,
				"default");

		// load message and stuff

		MessageRec messageIn =
			messageHelper.find (
				receivedMessage.getMessageId ());

		// work out ticket and message text

		String ticket =
			generateTicket (
				ticketChars,
				8);

		String messageText =
			ticketer.getText ().replaceAll (
				"\\{ticket\\}",
				ticket);

		// send message

		MessageRec messageOut =
			messageSender.get ()
				.threadId (messageIn.getThreadId ())
				.number (messageIn.getNumber ())
				.messageString (messageText)
				.numFrom (ticketer.getNumber ())
				.route (ticketer.getRoute ())
				.service (serviceHelper.findByCode (ticketer, "default"))
				.send ();

		// create ticketer ticket entry

		ticketerTicketHelper.insert (
			new TicketerTicketRec ()
				.setTicketer (ticketer)
				.setNumber (messageIn.getNumber ())
				.setTicket (ticket)
				.setMessage (messageOut));

		// process inbox
		
		inboxLogic.inboxProcessed (
			messageIn,
			defaultService,
			null,
			command);

		transaction.commit ();

	}

	private static final
	String ticketChars =
		"ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	private
	String generateTicket (
			String sourceChars,
			int length) {

		char[] chars =
			new char [length];

		for (
			int position = 0;
			position < length;
			position++
		) {

			chars [position] =
				sourceChars.charAt (
					random.nextInt (
						sourceChars.length ()));

		}

		return new String (chars);

	}

}
