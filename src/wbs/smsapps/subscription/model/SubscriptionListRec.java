package wbs.smsapps.subscription.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.DeletedField;
import wbs.framework.entity.annotations.DescriptionField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.NameField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class SubscriptionListRec
	implements CommonRecord<SubscriptionListRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	SubscriptionRec subscription;

	@CodeField
	String code;

	// details

	@NameField
	String name;

	@DescriptionField
	String description;

	@DeletedField
	Boolean deleted = false;

	// compre to

	public
	int compareTo (
			Record<SubscriptionListRec> otherRecord) {

		SubscriptionListRec other =
			(SubscriptionListRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getSubscription (),
				other.getSubscription ())

			.append (
				getCode (),
				other.getCode ())

			.toComparison ();

	}

}
