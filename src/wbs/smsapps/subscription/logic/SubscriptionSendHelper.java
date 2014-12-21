package wbs.smsapps.subscription.logic;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.isNull;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import org.joda.time.Instant;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
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
import wbs.sms.message.outbox.logic.MessageSender;
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
		GenericSendHelper<
			SubscriptionRec,
			SubscriptionSendRec,
			SubscriptionSendNumberRec
		> {

	// dependencies

	@Inject
	AffiliateObjectHelper affiliateHelper;

	@Inject
	BatchObjectHelper batchHelper;

	@Inject
	BatchLogic batchLogic;

	@Inject
	BatchSubjectObjectHelper batchSubjectHelper;

	@Inject
	Database database;

	@Inject
	ObjectTypeObjectHelper objectTypeHelper;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	SubscriptionBillObjectHelper subscriptionBillHelper;

	@Inject
	SubscriptionLogic subscriptionLogic;

	@Inject
	SubscriptionObjectHelper subscriptionHelper;

	@Inject
	SubscriptionNumberObjectHelper subscriptionNumberHelper;

	@Inject
	SubscriptionSendObjectHelper subscriptionSendHelper;

	@Inject
	SubscriptionSendNumberObjectHelper subscriptionSendNumberHelper;

	@Inject
	SubscriptionSendPartObjectHelper subscriptionSendPartHelper;

	@Inject
	SubscriptionSubObjectHelper subscriptionSubHelper;

	@Inject
	TextObjectHelper textHelper;

	// prototype dependencies

	@Inject
	Provider<MessageSender> messageSenderProvider;

	// details

	public
	String name() {
		return "subscription";
	}

	public
	String itemNamePlural () {
		return "subscription numbers";
	}

	public
	ObjectHelper<SubscriptionSendRec> jobHelper () {
		return subscriptionSendHelper;
	}

	public
	ObjectHelper<SubscriptionSendNumberRec> itemHelper () {
		return subscriptionSendNumberHelper;
	}

	public
	List<SubscriptionSendRec> findSendingJobs () {
		return subscriptionSendHelper.findSending ();
	}

	public
	List<SubscriptionSendRec> findScheduledJobs (
			Instant now) {

		return subscriptionSendHelper.findScheduled (
			now);

	}

	public
	List<SubscriptionSendNumberRec> findItemsLimit (
			SubscriptionRec subscription,
			SubscriptionSendRec subscriptionSend,
			int maxResults) {

		return subscriptionSendNumberHelper.findQueuedLimit (
			subscriptionSend,
			maxResults);

	}

	public
	SubscriptionRec getService (
			SubscriptionSendRec subscriptionSend) {

		return subscriptionSend.getSubscription ();

	}

	public
	Instant getScheduledTime (
			SubscriptionRec subscription,
			SubscriptionSendRec subscriptionSend) {

		return subscriptionSend.getScheduledForTime ();

	}

	public
	boolean jobScheduled (
			SubscriptionRec subscription,
			SubscriptionSendRec subscriptionSend) {

		return equal (
			subscriptionSend.getState (),
			SubscriptionSendState.scheduled);

	}

	public
	boolean jobSending (
			SubscriptionRec subscription,
			SubscriptionSendRec subscriptionSend) {

		return equal (
			subscriptionSend.getState (),
			SubscriptionSendState.sending);

	}

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

	public
	void sendStart (
			SubscriptionRec subscription,
			SubscriptionSendRec subscriptionSend) {

		Transaction transaction =
			database.currentTransaction ();

		// create a batch

		BatchSubjectRec batchSubject =
			batchLogic.batchSubject (
				subscription,
				"send");

		BatchRec batch =
			batchHelper.insert (
				new BatchRec ()

			.setParentObjectType (
				objectTypeHelper.find (
					subscriptionSendHelper.objectTypeId ()))

			.setParentObjectId (
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
				new SubscriptionSendNumberRec ()

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

	public
	boolean verifyItem (
			SubscriptionRec subscription,
			SubscriptionSendRec subscriptionSend,
			SubscriptionSendNumberRec subscriptionSendNumber) {

		return true;

	}

	public
	void rejectItem (
			SubscriptionRec subscription,
			SubscriptionSendRec subscriptionSend,
			SubscriptionSendNumberRec subscriptionSendNumber) {

		throw new UnsupportedOperationException ();

	}

	public
	void sendItem (
			SubscriptionRec subscription,
			SubscriptionSendRec subscriptionSend,
			SubscriptionSendNumberRec subscriptionSendNumber) {

		SubscriptionSubRec subscriptionSub =
			subscriptionSendNumber.getSubscriptionSub ();

		SubscriptionNumberRec subscriptionNumber =
			subscriptionSub.getSubscriptionNumber ();

		if (

			subscriptionNumber.getBalance ()
				>= subscription.getDebitsPerSend ()

		) {

			subscriptionLogic.sendNow (
				subscriptionSendNumber);

		} else {

			subscriptionLogic.sendLater (
				subscriptionSendNumber);

		}

	}

	public
	void sendComplete (
			SubscriptionRec subscription,
			SubscriptionSendRec subscriptionSend) {

		subscriptionSend

			.setState (
				SubscriptionSendState.sent);

	}

}
