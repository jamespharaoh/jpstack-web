package wbs.smsapps.subscription.logic;

import static wbs.utils.etc.EnumUtils.enumEqualSafe;
import static wbs.utils.etc.Misc.isNull;

import java.util.List;

import javax.inject.Provider;

import lombok.NonNull;

import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectHelper;

import wbs.platform.affiliate.model.AffiliateObjectHelper;
import wbs.platform.object.core.model.ObjectTypeObjectHelper;
import wbs.platform.send.GenericSendHelper;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.text.model.TextObjectHelper;

import wbs.sms.message.batch.logic.BatchLogic;
import wbs.sms.message.batch.model.BatchObjectHelper;
import wbs.sms.message.batch.model.BatchRec;
import wbs.sms.message.batch.model.BatchSubjectObjectHelper;
import wbs.sms.message.batch.model.BatchSubjectRec;
import wbs.sms.message.outbox.logic.SmsMessageSender;

import wbs.smsapps.subscription.model.SubscriptionBillObjectHelper;
import wbs.smsapps.subscription.model.SubscriptionListRec;
import wbs.smsapps.subscription.model.SubscriptionNumberObjectHelper;
import wbs.smsapps.subscription.model.SubscriptionNumberRec;
import wbs.smsapps.subscription.model.SubscriptionObjectHelper;
import wbs.smsapps.subscription.model.SubscriptionRec;
import wbs.smsapps.subscription.model.SubscriptionSendNumberObjectHelper;
import wbs.smsapps.subscription.model.SubscriptionSendNumberRec;
import wbs.smsapps.subscription.model.SubscriptionSendNumberState;
import wbs.smsapps.subscription.model.SubscriptionSendObjectHelper;
import wbs.smsapps.subscription.model.SubscriptionSendPartObjectHelper;
import wbs.smsapps.subscription.model.SubscriptionSendPartRec;
import wbs.smsapps.subscription.model.SubscriptionSendRec;
import wbs.smsapps.subscription.model.SubscriptionSendState;
import wbs.smsapps.subscription.model.SubscriptionSubObjectHelper;
import wbs.smsapps.subscription.model.SubscriptionSubRec;

@SingletonComponent ("subscriptionSendHelper")
public
class SubscriptionSendHelper
	implements
		GenericSendHelper <
			SubscriptionRec,
			SubscriptionSendRec,
			SubscriptionSendNumberRec
		> {

	// singleton dependencies

	@SingletonDependency
	AffiliateObjectHelper affiliateHelper;

	@SingletonDependency
	BatchObjectHelper batchHelper;

	@SingletonDependency
	BatchLogic batchLogic;

	@SingletonDependency
	BatchSubjectObjectHelper batchSubjectHelper;

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ObjectTypeObjectHelper objectTypeHelper;

	@SingletonDependency
	ServiceObjectHelper serviceHelper;

	@SingletonDependency
	SubscriptionBillObjectHelper subscriptionBillHelper;

	@SingletonDependency
	SubscriptionLogic subscriptionLogic;

	@SingletonDependency
	SubscriptionObjectHelper subscriptionHelper;

	@SingletonDependency
	SubscriptionNumberObjectHelper subscriptionNumberHelper;

	@SingletonDependency
	SubscriptionSendObjectHelper subscriptionSendHelper;

	@SingletonDependency
	SubscriptionSendNumberObjectHelper subscriptionSendNumberHelper;

	@SingletonDependency
	SubscriptionSendPartObjectHelper subscriptionSendPartHelper;

	@SingletonDependency
	SubscriptionSubObjectHelper subscriptionSubHelper;

	@SingletonDependency
	TextObjectHelper textHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider <SmsMessageSender> messageSenderProvider;

	// details

	@Override
	public
	String name () {
		return "subscription";
	}

	@Override
	public
	String itemNamePlural () {
		return "subscription numbers";
	}

	@Override
	public
	ObjectHelper<SubscriptionSendRec> jobHelper () {
		return subscriptionSendHelper;
	}

	@Override
	public
	ObjectHelper<SubscriptionSendNumberRec> itemHelper () {
		return subscriptionSendNumberHelper;
	}

	@Override
	public
	List<SubscriptionSendRec> findSendingJobs () {
		return subscriptionSendHelper.findSending ();
	}

	@Override
	public
	List<SubscriptionSendRec> findScheduledJobs (
			Instant now) {

		return subscriptionSendHelper.findScheduled (
			now);

	}

	@Override
	public
	List<SubscriptionSendNumberRec> findItemsLimit (
			SubscriptionRec subscription,
			SubscriptionSendRec subscriptionSend,
			int maxResults) {

		return subscriptionSendNumberHelper.findQueuedLimit (
			subscriptionSend,
			maxResults);

	}

	@Override
	public
	SubscriptionRec getService (
			SubscriptionSendRec subscriptionSend) {

		return subscriptionSend.getSubscription ();

	}

	@Override
	public
	Instant getScheduledTime (
			SubscriptionRec subscription,
			SubscriptionSendRec subscriptionSend) {

		return subscriptionSend.getScheduledForTime ();

	}

	@Override
	public
	boolean jobScheduled (
			SubscriptionRec subscription,
			SubscriptionSendRec subscriptionSend) {

		return enumEqualSafe (
			subscriptionSend.getState (),
			SubscriptionSendState.scheduled);

	}

	@Override
	public
	boolean jobSending (
			SubscriptionRec subscription,
			SubscriptionSendRec subscriptionSend) {

		return enumEqualSafe (
			subscriptionSend.getState (),
			SubscriptionSendState.sending);

	}

	@Override
	public
	boolean jobConfigured (
			SubscriptionRec subscription,
			SubscriptionSendRec subscriptionSend) {

		if (
			isNull (
				subscription.getBilledRoute ())
		) {
			return false;
		}

		return true;

	}

	@Override
	public
	void sendStart (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull SubscriptionRec subscription,
			@NonNull SubscriptionSendRec subscriptionSend) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"sendStart");

		Transaction transaction =
			database.currentTransaction ();

		// create a batch

		BatchSubjectRec batchSubject =
			batchLogic.batchSubject (
				taskLogger,
				subscription,
				"send");

		BatchRec batch =
			batchHelper.insert (
				taskLogger,
				batchHelper.createInstance ()

			.setParentType (
				objectTypeHelper.findRequired (
					subscriptionSendHelper.objectTypeId ()))

			.setParentId (
				subscriptionSend.getId ())

			.setSubject (
				batchSubject)

			.setCode (
				batchSubject.getCode ())

		);

		// update send

		subscriptionSend

			.setState (
				SubscriptionSendState.sending)

			.setSentTime (
				transaction.now ())

			.setBatch (
				batch);

		// create send numbers

		for (
			SubscriptionNumberRec subscriptionNumber
				: subscription.getActiveSubscriptionNumbers ()
		) {

			SubscriptionSubRec subscriptionSub =
				subscriptionNumber.getActiveSubscriptionSub ();

			SubscriptionListRec subscriptionList =
				subscriptionSub.getSubscriptionList ();

			SubscriptionSendPartRec subscriptionSendPart =
				subscriptionSend.getPartsByList ().get (
					subscriptionList.getId ());

			if (subscriptionSendPart == null)
				continue;

			subscriptionSendNumberHelper.insert (
				taskLogger,
				subscriptionSendNumberHelper.createInstance ()

				.setSubscriptionSend (
					subscriptionSend)

				.setNumber (
					subscriptionNumber.getNumber ())

				.setSubscriptionSub (
					subscriptionNumber.getActiveSubscriptionSub ())

				.setState (
					SubscriptionSendNumberState.queued)

			);

		}

	}

	@Override
	public
	boolean verifyItem (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull SubscriptionRec subscription,
			@NonNull SubscriptionSendRec subscriptionSend,
			@NonNull SubscriptionSendNumberRec subscriptionSendNumber) {

		return true;

	}

	@Override
	public
	void rejectItem (
			SubscriptionRec subscription,
			SubscriptionSendRec subscriptionSend,
			SubscriptionSendNumberRec subscriptionSendNumber) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	void sendItem (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull SubscriptionRec subscription,
			@NonNull SubscriptionSendRec subscriptionSend,
			@NonNull SubscriptionSendNumberRec subscriptionSendNumber) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"sendItem");

		SubscriptionSubRec subscriptionSub =
			subscriptionSendNumber.getSubscriptionSub ();

		SubscriptionNumberRec subscriptionNumber =
			subscriptionSub.getSubscriptionNumber ();

		if (

			subscriptionNumber.getBalance ()
				>= subscription.getDebitsPerSend ()

		) {

			subscriptionLogic.sendNow (
				taskLogger,
				subscriptionSendNumber);

		} else {

			subscriptionLogic.sendLater (
				taskLogger,
				subscriptionSendNumber);

		}

	}

	@Override
	public
	void sendComplete (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull SubscriptionRec subscription,
			@NonNull SubscriptionSendRec subscriptionSend) {

		subscriptionSend

			.setState (
				SubscriptionSendState.sent);

	}

}
