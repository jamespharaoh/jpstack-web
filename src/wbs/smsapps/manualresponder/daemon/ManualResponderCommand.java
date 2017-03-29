package wbs.smsapps.manualresponder.daemon;

import static wbs.utils.etc.LogicUtils.referenceNotEqualWithClass;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.Misc.lessThan;
import static wbs.utils.etc.Misc.shouldNeverHappen;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.time.TimeUtils.calculateAgeInYears;
import static wbs.utils.time.TimeUtils.earlierThan;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.Instant;
import org.joda.time.LocalDate;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectManager;

import wbs.platform.queue.logic.QueueLogic;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;

import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.core.logic.DateFinder;
import wbs.sms.customer.logic.SmsCustomerLogic;
import wbs.sms.customer.model.SmsCustomerManagerRec;
import wbs.sms.customer.model.SmsCustomerObjectHelper;
import wbs.sms.customer.model.SmsCustomerRec;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.logic.SmsInboxLogic;
import wbs.sms.message.inbox.model.InboxAttemptRec;
import wbs.sms.message.inbox.model.InboxRec;
import wbs.sms.number.list.logic.NumberListLogic;

import wbs.smsapps.manualresponder.logic.ManualResponderLogic;
import wbs.smsapps.manualresponder.model.ManualResponderAffiliateRec;
import wbs.smsapps.manualresponder.model.ManualResponderNumberObjectHelper;
import wbs.smsapps.manualresponder.model.ManualResponderNumberRec;
import wbs.smsapps.manualresponder.model.ManualResponderRec;
import wbs.smsapps.manualresponder.model.ManualResponderRequestObjectHelper;
import wbs.smsapps.manualresponder.model.ManualResponderRequestRec;

import wbs.utils.email.EmailLogic;
import wbs.utils.time.TimeFormatter;

@Accessors (fluent = true)
@PrototypeComponent ("manualResponderCommand")
public
class ManualResponderCommand
	implements CommandHandler {

	// singleton dependencies

	@SingletonDependency
	CommandObjectHelper commandHelper;

	@SingletonDependency
	Database database;

	@SingletonDependency
	EmailLogic emailLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ManualResponderLogic manualResponderLogic;

	@SingletonDependency
	ManualResponderNumberObjectHelper manualResponderNumberHelper;

	@SingletonDependency
	ManualResponderRequestObjectHelper manualResponderRequestHelper;

	@SingletonDependency
	MessageObjectHelper messageHelper;

	@SingletonDependency
	NumberListLogic numberListLogic;

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	QueueLogic queueLogic;

	@SingletonDependency
	ServiceObjectHelper serviceHelper;

	@SingletonDependency
	SmsCustomerObjectHelper smsCustomerHelper;

	@SingletonDependency
	SmsCustomerLogic smsCustomerLogic;

	@SingletonDependency
	SmsInboxLogic smsInboxLogic;

	@SingletonDependency
	TimeFormatter timeFormatter;

	@SingletonDependency
	WbsConfig wbsConfig;

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
			"manual_responder.default",
			"manual_responder_affiliate.default",
		};

	}

	// state

	Transaction transaction;

	ManualResponderRec manualResponder;
	ManualResponderAffiliateRec manualResponderAffiliate;
	MessageRec message;
	ManualResponderNumberRec manualResponderNumber;
	ServiceRec defaultService;
	Optional <SmsCustomerRec> smsCustomer;
	ManualResponderRequestRec request;
	Instant previousRequestTime;

	// implementation

	@Override
	public
	InboxAttemptRec handle (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"handle");

		transaction =
			database.currentTransaction ();

		Record <?> commandParent =
			objectManager.getParentRequired (
				command);

		if (commandParent instanceof ManualResponderRec) {

			manualResponder =
				(ManualResponderRec)
				commandParent;

		} else if (commandParent instanceof ManualResponderAffiliateRec) {

			manualResponderAffiliate =
				(ManualResponderAffiliateRec)
				commandParent;

			manualResponder =
				manualResponderAffiliate.getManualResponder ();

		} else {

			shouldNeverHappen ();

		}

		message =
			inbox.getMessage ();

		manualResponderNumber =
			manualResponderNumberHelper.findOrCreate (
				taskLogger,
				manualResponder,
				message.getNumber ());

		defaultService =
			serviceHelper.findByCodeRequired (
				manualResponder,
				"default");

		// remove from number list

		if (
			isNotNull (
				manualResponder.getUnblockNumberList ())
		) {

			numberListLogic.removeDueToMessage (
				taskLogger,
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
						taskLogger,
						manualResponder.getSmsCustomerManager (),
						message.getNumber ()));

			manualResponderNumber

				.setSmsCustomer (
					smsCustomer.get ());

		} else {

			smsCustomer =
				Optional.absent ();

		}

		// assign affiliate if appropriate

		if (

			isNotNull (
				manualResponder.getSmsCustomerManager ())

			&& isNotNull (
				manualResponderAffiliate)

		) {

			if (
				referenceNotEqualWithClass (
					SmsCustomerManagerRec.class,
					manualResponder
						.getSmsCustomerManager (),
					manualResponderAffiliate
						.getSmsCustomerAffiliate ()
						.getSmsCustomerManager ())
			) {

				shouldNeverHappen ();

			}

			smsCustomerLogic.customerAffiliateUpdate (
				taskLogger,
				smsCustomer.get (),
				manualResponderAffiliate.getSmsCustomerAffiliate (),
				message);

		}

		// save the request

		request =
			manualResponderRequestHelper.insert (
				taskLogger,
				manualResponderRequestHelper.createInstance ()

			.setManualResponderNumber (
				manualResponderNumber)

			.setIndex (
				manualResponderNumber.getNumRequests ())

			.setMessage (
				message)

			.setTimestamp (
				transaction.now ())

			.setNumber (
				message.getNumber ())

			.setPending (
				false)

		);

		// update number statistics

		previousRequestTime =
			manualResponderNumber.getLastRequest ();

		manualResponderNumber

			.setFirstRequest (
				ifNull (
					manualResponderNumber.getFirstRequest (),
					transaction.now ()))

			.setLastRequest (
				transaction.now ())

			.setNumRequests (
				manualResponderNumber.getNumRequests () + 1);

		// handle message as appropriate

		return handleGeneral (
			taskLogger);

	}

	InboxAttemptRec handleExpectDateOfBirth (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"handleExpectDateOfBirth");

		// reset if date of birth already known

		if (
			isNotNull (
				smsCustomer.get ().getDateOfBirth ())
		) {

			manualResponderNumber

				.setExpectDateOfBirth (
					false);

			return handleGeneral (
				taskLogger);

		}

		// interpret date of birth

		Optional<LocalDate> dateOfBirth =
			DateFinder.find (
				rest,
				1915);

		// error if it wasn't understood

		if (
			optionalIsNotPresent (
				dateOfBirth)
		) {

			emailLogic.sendSystemEmail (
				ImmutableList.of (
					wbsConfig.email ().developerAddress ()),
				"DOB error",
				stringFormat (

					"********** %s DOB ERROR **********\n",
					wbsConfig.name ().toUpperCase (),
					"\n",

					"Application:  manual responder\n",
					"Service:      %s.%s\n",
					manualResponder.getSlice ().getCode (),
					manualResponder.getCode (),
					"Timestamp:    %s\n",
					timeFormatter.timestampString (
						timeFormatter.timezone (
							ifNull (
								manualResponder
									.getSlice ()
									.getDefaultTimezone (),
								wbsConfig.defaultTimezone ())),
						transaction.now ()),
					"\n",

					"Message ID:   %s\n",
					integerToDecimalString (
						message.getId ()),
					"Route:        %s.%s\n",
					message.getRoute ().getSlice ().getCode (),
					message.getRoute ().getCode (),
					"Number from:  %s\n",
					message.getNumFrom (),
					"Number to:    %s\n",
					message.getNumTo (),
					"Full message: %s\n",
					message.getText ().getText (),
					"Message rest: %s\n",
					rest));

			return handleDateOfBirthError (
				taskLogger);

		}

		// store date of birth

		smsCustomer.get ()

			.setDateOfBirth (
				dateOfBirth.get ());

		manualResponderNumber

			.setExpectDateOfBirth (
				false);

		return handleGeneral (
			taskLogger);

	}

	InboxAttemptRec handleNeedDateOfBirth (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"handleNeedDateOfBirth");

		manualResponderLogic.sendTemplateAutomatically (
			taskLogger,
			request,
			manualResponder.getDateOfBirthTemplate ());

		manualResponderNumber

			.setExpectDateOfBirth (
				true);

		return smsInboxLogic.inboxProcessed (
			taskLogger,
			inbox,
			optionalOf (
				defaultService),
			manualResponderLogic.customerAffiliate (
				manualResponderNumber),
			command);

	}

	InboxAttemptRec handleDateOfBirthError (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"handleDateOfBirthError");

		manualResponderLogic.sendTemplateAutomatically (
			taskLogger,
			request,
			manualResponder.getDateOfBirthErrorTemplate ());

		manualResponderNumber

			.setExpectDateOfBirth (
				true);

		return smsInboxLogic.inboxProcessed (
			taskLogger,
			inbox,
			optionalOf (
				defaultService),
			manualResponderLogic.customerAffiliate (
				manualResponderNumber),
			command);

	}

	InboxAttemptRec handleTooYoungError (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"handleTooYoungError");

		manualResponderLogic.sendTemplateAutomatically (
			taskLogger,
			request,
			manualResponder.getTooYoungTemplate ());

		return smsInboxLogic.inboxProcessed (
			taskLogger,
			inbox,
			optionalOf (
				defaultService),
			manualResponderLogic.customerAffiliate (
				manualResponderNumber),
			command);

	}

	InboxAttemptRec handleGeneral (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"handleGeneral");

		// if expecting date of birth

		if (manualResponderNumber.getExpectDateOfBirth ()) {

			return handleExpectDateOfBirth (
				taskLogger);

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

				&& (

					isNull (
						previousRequestTime)

					|| earlierThan (
						previousRequestTime,
						transaction.now ().minus (
							Days.days (180).toStandardDuration ()))

				)

			) {

				return handleNeedDateOfBirth (
					taskLogger);

			}

			// error if too young

			if (
				isNotNull (
					smsCustomer.get ().getDateOfBirth ())
			) {

				Long age =
					calculateAgeInYears (
						smsCustomer.get ().getDateOfBirth (),
						transaction.now (),
						DateTimeZone.forID (
							wbsConfig.defaultTimezone ()));

				if (
					lessThan (
						age,
						manualResponder.getRequiredAge ())
				) {

					return handleTooYoungError (
						taskLogger);

				}

			}

		}

		return handleNormal (
			taskLogger);

	}

	InboxAttemptRec handleNormal (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"handleNormal");

		// start session

		if (
			optionalIsPresent (
				smsCustomer)
		) {

			smsCustomerLogic.sessionStart (
				taskLogger,
				smsCustomer.get (),
				optionalOf (
					message.getThreadId ()));

		}

		// create queue item

		QueueItemRec queueItem =
			queueLogic.createQueueItem (
				taskLogger,
				manualResponder,
				"default",
				manualResponderNumber,
				request,
				manualResponderNumber.getCode (),
				message.getText ().getText ());

		request

			.setPending (
				true)

			.setQueueItem (
				queueItem);

		// return

		return smsInboxLogic.inboxProcessed (
			taskLogger,
			inbox,
			optionalOf (
				defaultService),
			manualResponderLogic.customerAffiliate (
				manualResponderNumber),
			command);

	}

}
