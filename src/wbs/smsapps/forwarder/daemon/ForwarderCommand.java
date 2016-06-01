package wbs.smsapps.forwarder.daemon;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.logic.InboxLogic;
import wbs.sms.message.inbox.model.InboxAttemptRec;
import wbs.sms.message.inbox.model.InboxRec;
import wbs.sms.messageset.logic.MessageSetLogic;
import wbs.sms.messageset.model.MessageSetObjectHelper;
import wbs.smsapps.forwarder.model.ForwarderMessageInObjectHelper;
import wbs.smsapps.forwarder.model.ForwarderObjectHelper;
import wbs.smsapps.forwarder.model.ForwarderRec;

@Accessors (fluent = true)
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
	InboxLogic inboxLogic;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	MessageSetObjectHelper messageSetHelper;

	@Inject
	MessageSetLogic messageSetLogic;

	@Inject
	ServiceObjectHelper serviceHelper;

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
			"forwarder.forwarder"
		};

	}

	// implementation

	@Override
	public
	InboxAttemptRec handle () {

		Transaction transaction =
			database.currentTransaction ();

		ForwarderRec forwarder =
			forwarderHelper.find (
				command.getParentId ());

		MessageRec message =
			inbox.getMessage ();

		ServiceRec defaultService =
			serviceHelper.findByCodeOrNull (
				forwarder,
				"default");

		forwarderMessageInHelper.insert (
			forwarderMessageInHelper.createInstance ()

			.setForwarder (
				forwarder)

			.setNumber (
				message.getNumber ())

			.setMessage (
				message)

			.setSendQueue (
				forwarder.getUrl ().length () > 0)

			.setRetryTime (
				transaction.now ())

		);

		messageSetLogic.sendMessageSet (
			messageSetHelper.findByCodeOrNull (
				forwarder,
				"forwarder"),
			message.getThreadId (),
			message.getNumber (),
			defaultService);

		// process inbox

		return inboxLogic.inboxProcessed (
			inbox,
			Optional.of (defaultService),
			Optional.<AffiliateRec>absent (),
			command);

	}

}
