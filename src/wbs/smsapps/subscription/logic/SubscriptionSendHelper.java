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
import wbs.platform.send.GenericSendHelper;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.text.model.TextObjectHelper;
import wbs.sms.message.batch.model.BatchObjectHelper;
import wbs.sms.message.batch.model.BatchRec;
import wbs.sms.message.batch.model.BatchSubjectObjectHelper;
import wbs.sms.message.batch.model.BatchSubjectRec;
import wbs.sms.message.outbox.logic.MessageSender;
import wbs.smsapps.subscription.model.SubscriptionObjectHelper;
import wbs.smsapps.subscription.model.SubscriptionRec;
import wbs.smsapps.subscription.model.SubscriptionSendNumberObjectHelper;
import wbs.smsapps.subscription.model.SubscriptionSendNumberRec;
import wbs.smsapps.subscription.model.SubscriptionSendNumberState;
import wbs.smsapps.subscription.model.SubscriptionSendObjectHelper;
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
	BatchObjectHelper batchHelper;

	@Inject
	BatchSubjectObjectHelper batchSubjectHelper;

	@Inject
	Database database;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	SubscriptionLogic subscriptionLogic;

	@Inject
	SubscriptionObjectHelper subscriptionHelper;

	@Inject
	SubscriptionSendObjectHelper subscriptionSendHelper;

	@Inject
	SubscriptionSendNumberObjectHelper subscriptionSendNumberHelper;

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

		return subscriptionSendNumberHelper.findAcceptedLimit (
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

		if (isNull (
				subscription.getBilledRoute ()))
			return false;

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
			batchSubjectHelper.findByCode (
				subscription,
				"send");

		BatchRec batch =
			batchHelper.insert (
				new BatchRec ()

			.setSubject (
				batchSubject)

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

		List<SubscriptionSubRec> subscriptionSubs =
			subscriptionSubHelper.findActive (
				subscription);

		for (
			SubscriptionSubRec subscriptionSub
				: subscriptionSubs
		) {

			subscriptionSendNumberHelper.insert (
				new SubscriptionSendNumberRec ()

				.setSubscriptionSend (
					subscriptionSend)

				.setSubscriptionSub (
					subscriptionSub)

				.setState (
					SubscriptionSendNumberState.accepted)

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

		/*
		TemplateVersionRec templateVersion =
			subscriptionSend.getTemplate ().getTemplateVersion ();

		TextRec billedMessageText =
			templateVersion.getBilledEnabled ()
				? textHelper.findOrCreate (
					templateVersion.getBilledMessage ())
				: null;

		SubscriptionSubRec subscriptionSub =
			subscriptionSendNumber.getSubscriptionSub ();

		ServiceRec defaultService =
			serviceHelper.findByCode (
				subscription,
				"default");

		AffiliateRec affiliate =
			subscriptionLogic.getAffiliateForSubscriptionSub (
				subscriptionSub);

		if (templateVersion.getBilledEnabled ()) {

			// send the billed message

			MessageRec message =
				messageSenderProvider.get ()

				.number (
					subscriptionSub
						.getSubscriptionNumber ()
						.getNumber ())

				.messageText (
					billedMessageText)

				.numFrom (
					subscription.getBilledNumber ())

				.route (
					subscription.getBilledRoute ())

				.service (
					defaultService)

				.batch (
					subscriptionSend.getBatch ())

				.affiliate (
					affiliate)

				.deliveryTypeCode (
					"subscription")

				.ref (
					subscriptionSendNumber.getId ())

				.send ();

			subscriptionSendNumber

				.setBilledMessage (
					message)

				.setThreadId (
					message.getThreadId ())

				.setState (
					SubscriptionSendNumberState.halfSent);

		} else {

			// send the free messages

			for (
				TemplatePartRec templatePart
					: templateVersion.getTemplateParts ()
			) {

				MessageRec message =
					messageSenderProvider.get ()

					.threadId (
						subscriptionSendNumber.getThreadId ())

					.number (
						subscriptionSub
							.getSubscriptionNumber ()
							.getNumber ())

					.messageString (
						templatePart.getMessage ())

					.numFrom (
						subscription.getFreeNumber ())

					.route (
						subscription.getFreeRoute ())

					.service (
						defaultService)

					.batch (
						subscriptionSend.getBatch ())

					.affiliate (
						affiliate)

					.send ();

				subscriptionSendNumber

					.setThreadId (
						message.getThreadId ())

					.setState (
						SubscriptionSendNumberState.sent);

			}

		}
		*/

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
