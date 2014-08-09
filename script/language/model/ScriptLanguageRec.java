package wbs.platform.script.language.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.DescriptionField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.record.MajorRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MajorEntity
public
class ScriptLanguageRec
	implements MajorRecord<ScriptLanguageRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@CodeField
	String code;

	// details

	@DescriptionField
	String description;

	// compare to

	@Override
	public
	int compareTo (
			Record<ScriptLanguageRec> otherRecord) {

		ScriptLanguageRec other =
			(ScriptLanguageRec) otherRecord;

		return new CompareToBuilder ()
			.append (getCode (), other.getCode ())
			.toComparison ();

	}

}
