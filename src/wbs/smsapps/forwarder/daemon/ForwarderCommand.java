package wbs.smsapps.forwarder.daemon;

import java.util.Date;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.NonNull;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.daemon.ReceivedMessage;
import wbs.sms.messageset.logic.MessageSetLogic;
import wbs.smsapps.forwarder.model.ForwarderMessageInObjectHelper;
import wbs.smsapps.forwarder.model.ForwarderMessageInRec;
import wbs.smsapps.forwarder.model.ForwarderObjectHelper;
import wbs.smsapps.forwarder.model.ForwarderRec;

@SingletonComponent ("forwarderCommand")
public
class ForwarderCommand
	implements CommandHandler {

	// dependencies

	@Inject
	CommandObjectHelper commandHelper;

	@Inject
	Database database;

	@Inject
	ForwarderMessageInObjectHelper forwarderMessageInHelper;

	@Inject
	ForwarderObjectHelper forwarderHelper;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	MessageSetLogic messageSetLogic;

	@Inject
	ServiceObjectHelper serviceHelper;

	// details

	@Override
	public
	String[] getCommandTypes () {

		return new String [] {
			"forwarder.forwarder"
		};

	}

	// implementation

	@Override
	public
	Status handle (
			int commandId,
			@NonNull ReceivedMessage receivedMessage) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		CommandRec command =
			commandHelper.find (
				commandId);

		ForwarderRec forwarder =
			forwarderHelper.find (
				command.getParentObjectId ());

		MessageRec message =
			messageHelper.find (
				receivedMessage.getMessageId ());

		receivedMessage.setServiceId (
			serviceHelper.findByCode (forwarder, "default").getId ());

		forwarderMessageInHelper.insert (
			new ForwarderMessageInRec ()
				.setForwarder (forwarder)
				.setNumber (message.getNumber ())
				.setMessage (message)
				.setSendQueue (forwarder.getUrl ().length () > 0)
				.setRetryTime (new Date ()));

		messageSetLogic.sendMessageSet (
			messageSetLogic.findMessageSet (forwarder, "forwarder"),
			message.getThreadId (),
			message.getNumber (),
			serviceHelper.findByCode (forwarder, "default"));

		// return

		transaction.commit ();

		return CommandHandler.Status.processed;

	}

}
