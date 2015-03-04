package wbs.integrations.paypal.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.GeneratedIdField;
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
class PaypalPaymentRec
	implements CommonRecord<PaypalPaymentRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	PaypalAccountRec paypalAccount;

	// TODO ?

	// details

	@SimpleField
	Integer value;

	@SimpleField (
		nullable = true)
	String paypalToken;

	@SimpleField (
		nullable = true)
	String paypalPayerId;

	// state

	@SimpleField
	PaypalPaymentState state;

	@ReferenceField(
			nullable = true)
	PaypalPaymentRec paypalPayment;

	// compare to

	@Override
	public
	int compareTo (
			Record<PaypalPaymentRec> otherRecord) {

		PaypalPaymentRec other =
			(PaypalPaymentRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				other.getId (),
				getId ())

			.toComparison ();

	}

}
