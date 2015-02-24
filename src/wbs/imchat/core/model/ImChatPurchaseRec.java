package wbs.imchat.core.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.joda.time.Instant;

import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IndexField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.integrations.paypal.model.PaypalPaymentRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id" )
@MajorEntity
public
class ImChatPurchaseRec
	implements CommonRecord<ImChatPurchaseRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	ImChatCustomerRec imChatCustomer;

	@IndexField
	Integer index;

	// details

	@ReferenceField
	ImChatPricePointRec imChatPricePoint;

	@SimpleField
	Integer price;

	@SimpleField
	Integer value;

	@SimpleField
	Integer oldBalance;

	@SimpleField
	Integer newBalance;

	@SimpleField
	Instant timestamp;

	@ReferenceField(
			nullable = true)
	PaypalPaymentRec paypalPayment;

	// compare to

	@Override
	public
	int compareTo (
			Record<ImChatPurchaseRec> otherRecord) {

		ImChatPurchaseRec other =
			(ImChatPurchaseRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				other.getTimestamp (),
				getTimestamp ())

			.append (
				other.getId (),
				getId ())

			.toComparison ();

	}

}
