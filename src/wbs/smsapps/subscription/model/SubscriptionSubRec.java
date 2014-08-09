package wbs.smsapps.subscription.model;

import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.EphemeralEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.EphemeralRecord;
import wbs.framework.record.Record;
import wbs.platform.user.model.UserRec;
import wbs.sms.number.core.model.NumberRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@EphemeralEntity
public
class SubscriptionSubRec
	implements EphemeralRecord<SubscriptionSubRec> {

	// id

	@GeneratedIdField (
		sequence = "subscription_sub_id")
	Integer id;

	// identity

	@ReferenceField
	SubscriptionRec subscription;

	// TODO

	// details

	@ReferenceField
	NumberRec number;

	@ReferenceField
	SubscriptionAffiliateRec subscriptionAffiliate;

	@SimpleField
	Integer startedThreadId;

	@SimpleField
	Integer endedThreadId;

	@SimpleField
	Date started = new Date ();

	@SimpleField
	Date ended;

	@ReferenceField (
		column = "started_conuserid")
	UserRec startedBy;

	@ReferenceField (
		column = "ended_conuserid")
	UserRec endedBy;

	// state

	@SimpleField
	Boolean active = true;

	// compare to

	@Override
	public
	int compareTo (
			Record<SubscriptionSubRec> otherRecord) {

		SubscriptionSubRec other =
			(SubscriptionSubRec) otherRecord;

		return new CompareToBuilder ()
			.append (other.getStarted (), getStarted ())
			.append (other.getId (), getId ())
			.toComparison ();

	}

	// dao methods

	public static
	interface SubscriptionSubDaoMethods {

		SubscriptionSubRec findActive (
				SubscriptionRec subscription,
				NumberRec number);

		List<SubscriptionSubRec> findActive (
				SubscriptionRec subscription);

	}

}
