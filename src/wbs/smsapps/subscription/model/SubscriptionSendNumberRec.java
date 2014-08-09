package wbs.smsapps.subscription.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.number.core.model.NumberRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class SubscriptionSendNumberRec
	implements CommonRecord<SubscriptionSendNumberRec> {

	@GeneratedIdField
	Integer id;

	@ReferenceField
	SubscriptionSendRec subscriptionSend;

	@ReferenceField
	NumberRec number;

	@SimpleField
	Integer threadId;

	@ReferenceField
	MessageRec billedMessage;

	@SimpleField
	Boolean sent;

	@Override
	public
	int compareTo (
			Record<SubscriptionSendNumberRec> otherRecord) {

		SubscriptionSendNumberRec other =
			(SubscriptionSendNumberRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getSubscriptionSend (),
				other.getSubscriptionSend ())

			.append (
				getNumber (),
				other.getNumber ())

			.toComparison ();

	}

}
