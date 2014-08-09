package wbs.psychic.help.model;

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
import wbs.platform.text.model.TextRec;
import wbs.platform.user.model.UserRec;
import wbs.psychic.user.core.model.PsychicUserRec;
import wbs.sms.message.core.model.MessageRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class PsychicHelpRequestRec
	implements CommonRecord<PsychicHelpRequestRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	PsychicUserRec psychicUser;

	@IndexField
	Integer index;

	// data

	@ReferenceField
	MessageRec requestMessage;

	@ReferenceField (
		nullable = true)
	MessageRec responseMessage;

	@ReferenceField
	TextRec requestText;

	@ReferenceField (
		nullable = true)
	TextRec responseText;

	@SimpleField
	Instant requestTime;

	@SimpleField (
		nullable = true)
	Instant responseTime;

	@ReferenceField (
		nullable = true)
	UserRec responseUser;

	// compare to

	@Override
	public
	int compareTo (
			Record<PsychicHelpRequestRec> otherRecord) {

		PsychicHelpRequestRec other =
			(PsychicHelpRequestRec) otherRecord;

		return new CompareToBuilder ()
			.append (getPsychicUser (), other.getPsychicUser ())
			.append (getIndex (), other.getIndex ())
			.toComparison ();

	}

}
