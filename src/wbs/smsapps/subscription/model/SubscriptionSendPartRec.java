package wbs.smsapps.subscription.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.EphemeralEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IdentityReferenceField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.EphemeralRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@EphemeralEntity
public
class SubscriptionSendPartRec
	implements EphemeralRecord<SubscriptionSendPartRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	SubscriptionSendRec subscriptionSend;

	@IdentityReferenceField
	SubscriptionListRec subscriptionList;

	// details

	@SimpleField
	String text;

	// compare to

	@Override
	public
	int compareTo (
			Record<SubscriptionSendPartRec> otherRecord) {

		SubscriptionSendPartRec other =
			(SubscriptionSendPartRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getSubscriptionSend (),
				other.getSubscriptionSend ())

			.append (
				getSubscriptionList (),
				other.getSubscriptionList ())

			.toComparison ();

	}


}
