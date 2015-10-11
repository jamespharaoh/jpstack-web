package wbs.smsapps.broadcast.model;

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
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.platform.user.model.UserRec;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.number.core.model.NumberRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class BroadcastNumberRec
	implements CommonRecord<BroadcastNumberRec> {

	// identity

	@GeneratedIdField
	Integer id;

	@ParentField
	BroadcastRec broadcast;

	@IdentityReferenceField
	NumberRec number;

	// state

	@SimpleField
	BroadcastNumberState state;

	// other information

	@ReferenceField (
		nullable = true)
	UserRec addedByUser;

	@ReferenceField (
		nullable = true)
	UserRec removedByUser;

	@ReferenceField (
		nullable = true)
	MessageRec message;

	// compare to

	@Override
	public
	int compareTo (
			Record<BroadcastNumberRec> otherRecord) {

		BroadcastNumberRec other =
			(BroadcastNumberRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getBroadcast (),
				other.getBroadcast ())

			.append (
				getNumber (),
				other.getNumber ())

			.toComparison ();

	}

}
