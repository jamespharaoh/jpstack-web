package wbs.smsapps.manualresponder.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IdentityReferenceField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.platform.text.model.TextRec;
import wbs.sms.customer.model.SmsCustomerRec;
import wbs.sms.number.core.model.NumberRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class ManualResponderNumberRec
	implements CommonRecord<ManualResponderNumberRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	ManualResponderRec manualResponder;

	@IdentityReferenceField
	NumberRec number;

	// details

	@ReferenceField (
		nullable = true)
	SmsCustomerRec smsCustomer;

	// settings

	@ReferenceField
	TextRec notesText;

	// compare to

	@Override
	public
	int compareTo (
			Record<ManualResponderNumberRec> otherRecord) {

		ManualResponderNumberRec other =
			(ManualResponderNumberRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getManualResponder (),
				other.getManualResponder ())

			.append (
				getNumber (),
				other.getNumber ())

			.toComparison ();

	}

}
