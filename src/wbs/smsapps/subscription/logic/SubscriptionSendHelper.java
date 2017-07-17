package wbs.smsapps.subscription.logic;

import static wbs.utils.etc.EnumUtils.enumEqualSafe;
import static wbs.utils.etc.NullUtils.isNull;

import java.util.List;

import lombok.NonNull;

import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
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

	// details

	@Override
	public
	String parentTypeName () {
		return "subscription-send";
	}

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
	ObjectHelper <SubscriptionSendRec> jobHelper () {
		return subscriptionSendHelper;
	}

	@Override
	public
	ObjectHelper <SubscriptionSendNumberRec> itemHelper () {
		return subscriptionSendNumberHelper;
	}

	@Override
	public
	List <SubscriptionSendRec> findSendingJobs (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findSendingJobs");

		) {

			return subscriptionSendHelper.findSending (
				transaction);

		}

	}

	@Override
	public
	List <SubscriptionSendRec> findScheduledJobs (
			@NonNull Transaction parentTransaction,
			@NonNull Instant now) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findScheduledJobs");

		) {

			return subscriptionSendHelper.findScheduled (
				transaction,
				now);

		}

	}

	@Override
	public
	List <SubscriptionSendNumberRec> findItemsLimit (
			@NonNull Transaction parentTransaction,
			@NonNull SubscriptionRec subscription,
			@NonNull SubscriptionSendRec subscriptionSend,
			@NonNull Long maxResults) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findItemsLimit");

		) {

			return subscriptionSendNumberHelper.findQueuedLimit (
				transaction,
				subscriptionSend,
				maxResults);

		}

	}

	@Override
	public
	SubscriptionRec getService (
			@NonNull Transaction parentTransaction,
			@NonNull SubscriptionSendRec subscriptionSend) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getService");

		) {

			return subscriptionSend.getSubscription ();

		}

	}

	@Override
	public
	Instant getScheduledTime (
			@NonNull Transaction parentTransaction,
			@NonNull SubscriptionRec subscription,
			@NonNull SubscriptionSendRec subscriptionSend) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getScheduledTime");

		) {

			return subscriptionSend.getScheduledForTime ();

		}

	}

	@Override
	public
	boolean jobScheduled (
			@NonNull Transaction parentTransaction,
			@NonNull SubscriptionRec subscription,
			@NonNull SubscriptionSendRec subscriptionSend) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"jobScheduled");

		) {

			return enumEqualSafe (
				subscriptionSend.getState (),
				SubscriptionSendState.scheduled);

		}

	}

	@Override
	public
	boolean jobSending (
			@NonNull Transaction parentTransaction,
			@NonNull SubscriptionRec subscription,
			@NonNull SubscriptionSendRec subscriptionSend) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"jobSending");

		) {

			return enumEqualSafe (
				subscriptionSend.getState (),
				SubscriptionSendState.sending);

		}

	}

	@Override
	public
	boolean jobConfigured (
			@NonNull Transaction parentTransaction,
			@NonNull SubscriptionRec subscription,
			@NonNull SubscriptionSendRec subscriptionSend) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"jobConfigured");

		) {

			if (
				isNull (
					subscription.getBilledRoute ())
			) {
				return false;
			}

			return true;

		}

	}

	@Override
	public
	void sendStart (
			@NonNull Transaction parentTransaction,
			@NonNull SubscriptionRec subscription,
			@NonNull SubscriptionSendRec subscriptionSend) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"sendStart");

		) {

			// create a batch

			BatchSubjectRec batchSubject =
				batchLogic.batchSubject (
					transaction,
					subscription,
					"send");

			BatchRec batch =
				batchHelper.insert (
					transaction,
					batchHelper.createInstance ()

				.setParentType (
					objectTypeHelper.findRequired (
						transaction,
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
					transaction,
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

	}

	@Override
	public
	boolean verifyItem (
			@NonNull Transaction parentTransaction,
			@NonNull SubscriptionRec subscription,
			@NonNull SubscriptionSendRec subscriptionSend,
			@NonNull SubscriptionSendNumberRec subscriptionSendNumber) {

		return true;

	}

	@Override
	public
	void rejectItem (
			@NonNull Transaction parentTransaction,
			@NonNull SubscriptionRec subscription,
			@NonNull SubscriptionSendRec subscriptionSend,
			@NonNull SubscriptionSendNumberRec subscriptionSendNumber) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	void sendItem (
			@NonNull Transaction parentTransaction,
			@NonNull SubscriptionRec subscription,
			@NonNull SubscriptionSendRec subscriptionSend,
			@NonNull SubscriptionSendNumberRec subscriptionSendNumber) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"sendItem");

		) {

			SubscriptionSubRec subscriptionSub =
				subscriptionSendNumber.getSubscriptionSub ();

			SubscriptionNumberRec subscriptionNumber =
				subscriptionSub.getSubscriptionNumber ();

			if (

				subscriptionNumber.getBalance ()
					>= subscription.getDebitsPerSend ()

			) {

				subscriptionLogic.sendNow (
					transaction,
					subscriptionSendNumber);

			} else {

				subscriptionLogic.sendLater (
					transaction,
					subscriptionSendNumber);

			}

		}

	}

	@Override
	public
	void sendComplete (
			@NonNull Transaction parentTransaction,
			@NonNull SubscriptionRec subscription,
			@NonNull SubscriptionSendRec subscriptionSend) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"sendComplete");

		) {

			subscriptionSend

				.setState (
					SubscriptionSendState.sent);

		}

	}

}
