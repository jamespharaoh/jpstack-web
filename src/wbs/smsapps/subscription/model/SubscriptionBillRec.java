package wbs.smsapps.subscription.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.joda.time.Instant;

import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IndexField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.sms.message.core.model.MessageRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class SubscriptionBillRec
	implements CommonRecord<SubscriptionBillRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	SubscriptionNumberRec subscriptionNumber;

	@IndexField
	Integer index;

	// details

	@ReferenceField (
		nullable = true)
	MessageRec message;

	// times

	@SimpleField
	Instant createdTime;

	@SimpleField (
		nullable = true)
	Instant deliveredTime;

	// state

	@SimpleField
	SubscriptionBillState state;

	// compare to

	@Override
	public
	int compareTo (
			Record<SubscriptionBillRec> otherRecord) {

		SubscriptionBillRec other =
			(SubscriptionBillRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				other.getCreatedTime (),
				getCreatedTime ())

			.append (
				other.getId (),
				getId ())

			.toComparison ();

	}

}
