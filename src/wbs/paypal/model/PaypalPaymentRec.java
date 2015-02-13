package wbs.paypal.model;

import org.apache.commons.lang3.builder.CompareToBuilder;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IndexField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;

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

	@IndexField
	Integer index;
	
	@SimpleField
	String status;	
	
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
