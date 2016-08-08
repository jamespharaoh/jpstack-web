package wbs.smsapps.autoresponder.daemon;

import static wbs.framework.utils.etc.Misc.isNotNull;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.config.WbsConfig;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.utils.EmailLogic;
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
import wbs.sms.message.outbox.logic.MessageSender;
import wbs.sms.messageset.model.MessageSetMessageRec;
import wbs.sms.messageset.model.MessageSetObjectHelper;
import wbs.sms.messageset.model.MessageSetRec;
import wbs.sms.number.list.logic.NumberListLogic;
import wbs.smsapps.autoresponder.model.AutoResponderObjectHelper;
import wbs.smsapps.autoresponder.model.AutoResponderRec;
import wbs.smsapps.autoresponder.model.AutoResponderRequestObjectHelper;
import wbs.smsapps.autoresponder.model.AutoResponderRequestRec;

@Accessors (fluent = true)
@PrototypeComponent ("autoResponderCommand")
public
class AutoResponderCommand
	implements CommandHandler {

	// dependencies

	@Inject
	AutoResponderObjectHelper autoResponderHelper;

	@Inject
	AutoResponderRequestObjectHelper autoResponderRequestHelper;

	@Inject
	CommandObjectHelper commandHelper;

	@Inject
	Database database;

	@Inject
	EmailLogic emailLogic;

	@Inject
	SmsInboxLogic smsInboxLogic;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	MessageSetObjectHelper messageSetHelper;

	@Inject
	NumberListLogic numberListLogic;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	WbsConfig wbsConfig;

	// prototype dependencies

	@Inject
	Provider<MessageSender> messageSenderProvider;

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
			"auto_responder.default"
		};

	}

	@Override
	public
	InboxAttemptRec handle () {

		Transaction transaction =
			database.currentTransaction ();

		MessageRec receivedMessage =
			inbox.getMessage ();

		AutoResponderRec autoResponder =
			autoResponderHelper.findRequired (
				command.getParentId ());

		ServiceRec defaultService =
			serviceHelper.findByCodeRequired (
				autoResponder,
				"default");

		// create request

		AutoResponderRequestRec request =
			autoResponderRequestHelper.insert (
				autoResponderRequestHelper.createInstance ()

			.setAutoResponder (
				autoResponder)

			.setTimestamp (
				transaction.now ())

			.setNumber (
				receivedMessage.getNumber ())

			.setReceivedMessage (
				receivedMessage)

		);

		// send responses

		MessageSetRec messageSet =
			messageSetHelper.findByCodeRequired (
				autoResponder,
				"default");

		for (
			MessageSetMessageRec messageSetMessage
				: messageSet.getMessages ()
		) {

			MessageRec sentMessage =
				messageSenderProvider.get ()

				.threadId (
					receivedMessage.getThreadId ())

				.number (
					receivedMessage.getNumber ())

				.messageString (
					messageSetMessage.getMessage ())

				.numFrom (
					messageSetMessage.getNumber ())

				.route (
					messageSetMessage.getRoute ())

				.service (
					defaultService)

				.sendNow (
					! autoResponder.getSequenceResponses ()
					|| messageSetMessage.getIndex () == 0)

				.deliveryTypeCode (
					"auto_responder")

				.ref (
					(long) (int) request.getId ())

				.send ();

			request.getSentMessages ().add (
				sentMessage);

		}

		// send email

		if (
			autoResponder.getEmailAddress () != null
			&& autoResponder.getEmailAddress ().length () > 0
		) {

			emailLogic.sendSystemEmail (
				ImmutableList.of (
					autoResponder.getEmailAddress ()),
				"Auto responder " + autoResponder.getDescription (),
				receivedMessage.getText ().getText ());

		}

		// add to number list

		if (
			isNotNull (
				autoResponder.getAddToNumberList ())
		) {

			numberListLogic.addDueToMessage (
				autoResponder.getAddToNumberList (),
				receivedMessage.getNumber (),
				receivedMessage,
				defaultService);

		}

		// process inbox

		return smsInboxLogic.inboxProcessed (
			inbox,
			Optional.of (defaultService),
			Optional.<AffiliateRec>absent (),
			command);

	}

}
