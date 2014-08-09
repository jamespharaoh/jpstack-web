package wbs.psychic.keyword.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.EphemeralEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.EphemeralRecord;
import wbs.framework.record.Record;
import wbs.psychic.affiliate.model.PsychicAffiliateRec;
import wbs.psychic.core.model.PsychicRec;
import wbs.sms.command.model.CommandRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@EphemeralEntity
public
class PsychicKeywordRec
	implements EphemeralRecord<PsychicKeywordRec> {

	@GeneratedIdField
	Integer id;

	@ParentField
	PsychicRec psychic;

	@CodeField
	String keyword;

	@SimpleField
	PsychicKeywordType type;

	@ReferenceField (
		nullable = true)
	CommandRec targetCommand;

	@ReferenceField (
		nullable = true)
	PsychicAffiliateRec joinAffiliate;

	// compare to

	@Override
	public
	int compareTo (
			Record<PsychicKeywordRec> otherRecord) {

		PsychicKeywordRec other =
			(PsychicKeywordRec) otherRecord;

		return new CompareToBuilder ()
			.append (getPsychic (), other.getPsychic ())
			.append (getKeyword (), other.getKeyword ())
			.toComparison ();

	}

}
