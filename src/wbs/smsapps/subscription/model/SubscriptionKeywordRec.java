package wbs.smsapps.subscription.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.DeletedField;
import wbs.framework.entity.annotations.DescriptionField;
import wbs.framework.entity.annotations.EphemeralEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.record.MajorRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@EphemeralEntity
public
class SubscriptionKeywordRec
	implements MajorRecord<SubscriptionKeywordRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	SubscriptionRec subscription;

	@CodeField
	String keyword;

	// details

	@DescriptionField
	String description;

	@DeletedField
	Boolean deleted = false;

	// settings

	@ReferenceField (
		nullable = true)
	SubscriptionAffiliateRec subscriptionAffiliate;

	@ReferenceField (
		nullable = true)
	SubscriptionListRec subscriptionList;

	// compare to

	public
	int compareTo (
			Record<SubscriptionKeywordRec> otherRecord) {

		SubscriptionKeywordRec other =
			(SubscriptionKeywordRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getSubscription (),
				other.getSubscription ())

			.append (
				getKeyword (),
				other.getKeyword ())

			.toComparison ();

	}

}
