package wbs.smsapps.subscription.model;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.joda.time.Instant;

import wbs.framework.entity.annotations.CollectionField;
import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.DescriptionField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IndexField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.object.AbstractObjectHooks;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.platform.user.model.UserRec;
import wbs.sms.message.batch.model.BatchRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class SubscriptionSendRec
	implements CommonRecord<SubscriptionSendRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	SubscriptionRec subscription;

	@IndexField
	Integer index;

	// details

	@DescriptionField
	String description;

	@ReferenceField (
		nullable = true)
	BatchRec batch;

	// settings

	/*
	@ReferenceField
	TemplateRec template;
	*/

	// users

	@ReferenceField
	UserRec createdUser;

	@ReferenceField (
		nullable = true)
	UserRec sentUser;

	@ReferenceField (
		nullable = true)
	UserRec cancelledUser;

	// times

	@SimpleField
	Instant createdTime;

	@SimpleField (
		nullable = true)
	Instant sentTime;

	@SimpleField (
		nullable = true)
	Instant cancelledTime;

	@SimpleField (
		nullable = true)
	Instant scheduledTime;

	@SimpleField (
		nullable = true)
	Instant scheduledForTime;

	// statistics

	@SimpleField
	Integer numRecipients = 0;

	// state

	@SimpleField
	SubscriptionSendState state =
		SubscriptionSendState.notSent;

	// children

	@CollectionField (
		index = "index")
	List<SubscriptionSendPartRec> parts;

	// compare to

	@Override
	public
	int compareTo (
			Record<SubscriptionSendRec> otherRecord) {

		SubscriptionSendRec other =
			(SubscriptionSendRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				other.getCreatedTime (),
				getCreatedTime ())

			.append (
				other.getId (),
				getId ())

			.toComparison ();

	}

	// dao methods

	public static
	interface SubscriptionSendDaoMethods {

		List<SubscriptionSendRec> findSending ();

		List<SubscriptionSendRec> findScheduled (
				Instant now);

	}

	// object hooks

	public static
	class SubscriptionSendHooks
		extends AbstractObjectHooks<SubscriptionSendRec> {

		// implementation

		@Override
		public
		void beforeInsert (
				SubscriptionSendRec subscriptionSend) {

			SubscriptionRec subscription =
				subscriptionSend.getSubscription ();

			// set index

			subscriptionSend

				.setIndex (
					subscription.getNumSendsTotal ());

		}

		@Override
		public
		void afterInsert (
				SubscriptionSendRec subscriptionSend) {

			SubscriptionRec subscription =
				subscriptionSend.getSubscription ();

			// update parent counts

			subscription

				.setNumSendsTotal (
					subscription.getNumSendsTotal () + 1);

		}

	}

}
