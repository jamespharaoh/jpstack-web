package wbs.psychic.template.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MinorEntity;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.record.MinorRecord;
import wbs.framework.record.Record;
import wbs.platform.text.model.TextRec;
import wbs.psychic.core.model.PsychicRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MinorEntity
public
class PsychicTemplateRec
	implements MinorRecord<PsychicTemplateRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	PsychicRec psychic;

	@CodeField
	String code;

	// details

	@ReferenceField
	PsychicTemplateTypeRec psychicTemplateType;

	// settings

	@ReferenceField
	TextRec templateText;

	// compare to

	@Override
	public
	int compareTo (
			Record<PsychicTemplateRec> otherRecord) {

		PsychicTemplateRec other =
			(PsychicTemplateRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getPsychic (),
				other.getPsychic ())

			.append (
				getCode (),
				other.getCode ())

			.toComparison ();

	}

}
