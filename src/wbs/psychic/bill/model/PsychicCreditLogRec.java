package wbs.psychic.bill.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.joda.time.Instant;

import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.platform.text.model.TextRec;
import wbs.platform.user.model.UserRec;
import wbs.psychic.user.core.model.PsychicUserRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class PsychicCreditLogRec
	implements CommonRecord<PsychicCreditLogRec> {

	@GeneratedIdField
	Integer id;

	@ParentField
	PsychicUserRec psychicUser;

	@SimpleField
	Instant timestamp;

	@ReferenceField
	UserRec user;

	@SimpleField
	Integer creditAmount;

	@SimpleField
	Integer paymentAmount;

	@ReferenceField
	TextRec detailsText;

	@SimpleField
	Boolean gift;

	// compare to

	@Override
	public
	int compareTo (
			Record<PsychicCreditLogRec> otherRecord) {

		PsychicCreditLogRec other =
			(PsychicCreditLogRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getPsychicUser (),
				other.getPsychicUser ())

			.append (
				other.getTimestamp (),
				getTimestamp ())

			.toComparison ();

	}

}
