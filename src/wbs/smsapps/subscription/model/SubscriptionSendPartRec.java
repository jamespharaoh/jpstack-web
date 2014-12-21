package wbs.smsapps.subscription.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.DeletedField;
import wbs.framework.entity.annotations.EphemeralEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IdentityReferenceField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.record.MinorRecord;
import wbs.framework.record.Record;
import wbs.platform.text.model.TextRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@EphemeralEntity
public
class SubscriptionSendPartRec
	implements MinorRecord<SubscriptionSendPartRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	SubscriptionSendRec subscriptionSend;

	@IdentityReferenceField
	SubscriptionListRec subscriptionList;

	// details

	@DeletedField
	Boolean deleted = false;

	// settings

	@ReferenceField
	TextRec text;

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

	// dao methods

	public static
	interface SubscriptionSendPartDaoMethods {

		SubscriptionSendPartRec find (
				SubscriptionSendRec subscriptionSend,
				SubscriptionListRec subscriptionList);

	}

}
