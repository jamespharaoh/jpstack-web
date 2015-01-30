package wbs.smsapps.manualresponder.daemon;

import static wbs.framework.utils.etc.Misc.isNotNull;

import java.util.Date;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.object.ObjectManager;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.queue.logic.QueueLogic;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.customer.logic.SmsCustomerLogic;
import wbs.sms.customer.model.SmsCustomerObjectHelper;
import wbs.sms.customer.model.SmsCustomerRec;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.logic.InboxLogic;
import wbs.sms.message.inbox.model.InboxAttemptRec;
import wbs.sms.message.inbox.model.InboxRec;
import wbs.sms.number.list.logic.NumberListLogic;
import wbs.smsapps.manualresponder.model.ManualResponderRec;
import wbs.smsapps.manualresponder.model.ManualResponderRequestObjectHelper;
import wbs.smsapps.manualresponder.model.ManualResponderRequestRec;

import com.google.common.base.Optional;

@Accessors (fluent = true)
@PrototypeComponent ("manualResponderCommand")
public
class ManualResponderCommand
	implements CommandHandler {

	// dependencies

	@Inject
	CommandObjectHelper commandHelper;

	@Inject
	Database database;

	@Inject
	InboxLogic inboxLogic;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	ManualResponderRequestObjectHelper manualResponderRequestHelper;

	@Inject
	NumberListLogic numberListLogic;

	@Inject
	ObjectManager objectManager;

	@Inject
	QueueLogic queueLogic;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	SmsCustomerObjectHelper smsCustomerHelper;

	@Inject
	SmsCustomerLogic smsCustomerLogic;

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
			"manual_responder.default"
		};

	}

	// implementation

	@Override
	public
	InboxAttemptRec handle () {

		ManualResponderRec manualResponder =
			(ManualResponderRec) (Object)
			objectManager.getParent (
				command);

		MessageRec message =
			inbox.getMessage ();

		// save the message value and service

		ServiceRec defaultService =
			serviceHelper.findByCode (
				manualResponder,
				"default");

		// remove from number list

		if (
			isNotNull (
				manualResponder.getUnblockNumberList ())
		) {

			numberListLogic.removeDueToMessage (
				manualResponder.getUnblockNumberList (),
				message.getNumber (),
				message,
				defaultService);

		}

		// hook into customer manager

		if (manualResponder.getSmsCustomerManager () != null) {

			SmsCustomerRec customer =
				smsCustomerHelper.findOrCreate (
					manualResponder.getSmsCustomerManager (),
					message.getNumber ());

			smsCustomerLogic.sessionStart (
				customer,
				Optional.of (
					message.getThreadId ()));

		}

		// save the request

		ManualResponderRequestRec request =
			manualResponderRequestHelper.insert (
				new ManualResponderRequestRec ()

			.setManualResponder (
				manualResponder)

			.setMessage (
				message)

			.setTimestamp (
				new Date ())

			.setPending (
				true)

			.setNumber (
				message.getNumber ())

		);

		QueueItemRec queueItem =
			queueLogic.createQueueItem (
				queueLogic.findQueue (
					manualResponder,
					"default"),
				message.getNumber (),
				request,
				message.getNumFrom (),
				message.getText ().getText ());

		request

			.setQueueItem (
				queueItem);

		return inboxLogic.inboxProcessed (
			inbox,
			Optional.of (defaultService),
			Optional.<AffiliateRec>absent (),
			command);

	}

}
