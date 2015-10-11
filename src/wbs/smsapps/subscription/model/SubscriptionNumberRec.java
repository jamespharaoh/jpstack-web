package wbs.smsapps.subscription.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.joda.time.Instant;

import wbs.framework.entity.annotations.CollectionField;
import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IdentityReferenceField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.sms.number.core.model.NumberRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class SubscriptionNumberRec
	implements CommonRecord<SubscriptionNumberRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	SubscriptionRec subscription;

	@IdentityReferenceField
	NumberRec number;

	// details

	@SimpleField (
		nullable = true)
	Instant firstJoin;

	@SimpleField (
		nullable = true)
	Instant lastJoin;

	@ReferenceField (
		nullable = true)
	SubscriptionListRec subscriptionList;

	@ReferenceField (
		nullable = true)
	SubscriptionAffiliateRec subscriptionAffiliate;

	@SimpleField
	Integer numSubs = 0;

	@SimpleField
	Integer numBills = 0;

	@SimpleField
	Integer balance = 0;

	// state

	@SimpleField
	Boolean active = false;

	@ReferenceField (
		nullable = true)
	SubscriptionSubRec activeSubscriptionSub;

	@ReferenceField (
		nullable = true)
	SubscriptionBillRec pendingSubscriptionBill;

	@ReferenceField (
		nullable = true)
	SubscriptionSendNumberRec pendingSubscriptionSendNumber;

	// children

	@CollectionField (
		index = "index")
	List<SubscriptionSubRec> subscriptionSubs =
		new ArrayList<SubscriptionSubRec> ();

	// compare to

	@Override
	public
	int compareTo (
			Record<SubscriptionNumberRec> otherRecord) {

		SubscriptionNumberRec other =
			(SubscriptionNumberRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getSubscription (),
				other.getSubscription ())

			.append (
				getNumber (),
				other.getNumber ())

			.toComparison ();

	}

}
