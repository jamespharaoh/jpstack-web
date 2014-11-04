package wbs.psychic.request.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.joda.time.Instant;

import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.GreatGrandParentField;
import wbs.framework.entity.annotations.IndexField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.platform.text.model.TextRec;
import wbs.platform.user.model.UserRec;
import wbs.psychic.contact.model.PsychicContactRec;
import wbs.psychic.core.model.PsychicRec;
import wbs.sms.message.core.model.MessageRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class PsychicRequestRec
	implements CommonRecord<PsychicRequestRec> {

	@GeneratedIdField
	Integer id;

	@ParentField
	PsychicContactRec psychicContact;

	@IndexField
	Integer index;

	@GreatGrandParentField
	PsychicRec psychic;

	@ReferenceField
	MessageRec requestMessage;

	@ReferenceField (
		nullable = true)
	MessageRec responseMessage;

	@SimpleField
	Instant requestTime;

	@SimpleField (
		nullable = true)
	Instant responseTime;

	@ReferenceField
	TextRec requestText;

	@ReferenceField (
		nullable = true)
	TextRec responseText;

	@ReferenceField (
		nullable = true)
	UserRec user;

	// compare to

	@Override
	public
	int compareTo (
			Record<PsychicRequestRec> otherRecord) {

		PsychicRequestRec other =
			(PsychicRequestRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getPsychicContact (),
				other.getPsychicContact ())

			.append (
				getIndex (),
				other.getIndex ())

			.toComparison ();

	}

}
