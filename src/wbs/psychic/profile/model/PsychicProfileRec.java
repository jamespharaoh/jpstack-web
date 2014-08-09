package wbs.psychic.profile.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.DescriptionField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.NameField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.record.MajorRecord;
import wbs.framework.record.Record;
import wbs.psychic.core.model.PsychicRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MajorEntity
public
class PsychicProfileRec
	implements MajorRecord<PsychicProfileRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	PsychicRec psychic;

	@CodeField
	String code;

	// details

	@NameField
	String name;

	@DescriptionField
	String info;

	// compare to

	@Override
	public
	int compareTo (
			Record<PsychicProfileRec> otherRecord) {

		PsychicProfileRec other =
			(PsychicProfileRec) otherRecord;

		return new CompareToBuilder ()
			.append (getPsychic (), other.getPsychic ())
			.append (getCode (), other.getCode ())
			.toComparison ();

	}

}
