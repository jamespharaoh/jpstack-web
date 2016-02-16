package wbs.smsapps.manualresponder.daemon;

import static wbs.framework.utils.etc.Misc.instantToDate;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.isNotPresent;
import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.Misc.isPresent;
import static wbs.framework.utils.etc.Misc.lessThan;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.Years;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.ObjectManager;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.queue.logic.QueueLogic;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.core.logic.DateFinder;
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
import wbs.smsapps.manualresponder.logic.ManualResponderLogic;
import wbs.smsapps.manualresponder.model.ManualResponderNumberObjectHelper;
import wbs.smsapps.manualresponder.model.ManualResponderNumberRec;
import wbs.smsapps.manualresponder.model.ManualResponderRec;
import wbs.smsapps.manualresponder.model.ManualResponderRequestObjectHelper;
import wbs.smsapps.manualresponder.model.ManualResponderRequestRec;

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
	ManualResponderLogic manualResponderLogic;

	@Inject
	ManualResponderNumberObjectHelper manualResponderNumberHelper;

	@Inject
	ManualResponderRequestObjectHelper manualResponderRequestHelper;

	@Inject
	MessageObjectHelper messageHelper;

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
	Optional<Long> commandRef;

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

	// state

	Transaction transaction;

	ManualResponderRec manualResponder;
	MessageRec message;
	ManualResponderNumberRec manualResponderNumber;
	ServiceRec defaultService;
	Optional<SmsCustomerRec> smsCustomer;
	ManualResponderRequestRec request;

	// implementation

	@Override
	public
	InboxAttemptRec handle () {

		transaction =
			database.currentTransaction ();

		manualResponder =
			(ManualResponderRec) (Object)
			objectManager.getParent (
				command);

		message =
			inbox.getMessage ();

		manualResponderNumber =
			manualResponderNumberHelper.findOrCreate (
				manualResponder,
				message.getNumber ());

		defaultService =
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

		// lookup sms customer

		if (
			isNotNull (
				manualResponder.getSmsCustomerManager ())
		) {

			smsCustomer =
				Optional.of (
					smsCustomerHelper.findOrCreate (
						manualResponder.getSmsCustomerManager (),
						message.getNumber ()));

			manualResponderNumber

				.setSmsCustomer (
					smsCustomer.get ());

		} else {

			smsCustomer =
				Optional.absent ();

		}

		// save the request

		request =
			manualResponderRequestHelper.insert (
				manualResponderRequestHelper.createInstance ()

			.setManualResponder (
				manualResponder)

			.setMessage (
				message)

			.setTimestamp (
				instantToDate (
					transaction.now ()))

			.setNumber (
				message.getNumber ())

			.setPending (
				false)

		);

		// handle message as appropriate

		return handleGeneral ();

	}

	InboxAttemptRec handleExpectDateOfBirth () {

		// reset if date of birth already known

		if (
			isNotNull (
				smsCustomer.get ().getDateOfBirth ())
		) {

			manualResponderNumber

				.setExpectDateOfBirth (
					false);

			return handleGeneral ();

		}

		// interpret date of birth

		Optional<LocalDate> dateOfBirth =
			DateFinder.find (
				rest,
				1915);

		// error if it wasn't understood

		if (
			isNotPresent (
				dateOfBirth)
		) {
			return handleDateOfBirthError ();
		}

		// store date of birth

		smsCustomer.get ()

			.setDateOfBirth (
				dateOfBirth.get ());

		manualResponderNumber

			.setExpectDateOfBirth (
				false);

		return handleGeneral ();

	}

	InboxAttemptRec handleNeedDateOfBirth () {

		manualResponderLogic.sendTemplateAutomatically (
			request,
			manualResponder.getDateOfBirthTemplate ());

		manualResponderNumber

			.setExpectDateOfBirth (
				true);

		return inboxLogic.inboxProcessed (
			inbox,
			Optional.of (defaultService),
			Optional.<AffiliateRec>absent (),
			command);

	}

	InboxAttemptRec handleDateOfBirthError () {

		manualResponderLogic.sendTemplateAutomatically (
			request,
			manualResponder.getDateOfBirthErrorTemplate ());

		manualResponderNumber

			.setExpectDateOfBirth (
				true);

		return inboxLogic.inboxProcessed (
			inbox,
			Optional.of (defaultService),
			Optional.<AffiliateRec>absent (),
			command);

	}

	InboxAttemptRec handleTooYoungError () {

		manualResponderLogic.sendTemplateAutomatically (
			request,
			manualResponder.getTooYoungTemplate ());

		return inboxLogic.inboxProcessed (
			inbox,
			Optional.of (defaultService),
			Optional.<AffiliateRec>absent (),
			command);

	}

	InboxAttemptRec handleGeneral () {

		// if expecting date of birth

		if (manualResponderNumber.getExpectDateOfBirth ()) {
			return handleExpectDateOfBirth ();
		}

		// check age

		if (
			isNotNull (
				manualResponder.getRequiredAge ())
		) {

			// ask for age

			if (
				isNull (
					smsCustomer.get ().getDateOfBirth ())
			) {
				return handleNeedDateOfBirth ();
			}

			// error if too young

			Integer age =
				Years.yearsBetween (
					smsCustomer.get ().getDateOfBirth ().toDateTimeAtStartOfDay (
						DateTimeZone.forID (
							"Europe/London")),
					transaction.now ()
				).getYears ();

			if (
				lessThan (
					age,
					manualResponder.getRequiredAge ())
			) {
				return handleTooYoungError ();
			}

		}

		return handleNormal ();

	}

	InboxAttemptRec handleNormal () {

		// start session

		if (
			isPresent (
				smsCustomer)
		) {

			smsCustomerLogic.sessionStart (
				smsCustomer.get (),
				Optional.of (
					message.getThreadId ()));

		}

		// create queue item

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

			.setPending (
				true)

			.setQueueItem (
				queueItem);

		// return

		return inboxLogic.inboxProcessed (
			inbox,
			Optional.of (defaultService),
			Optional.<AffiliateRec>absent (),
			command);

	}

}
