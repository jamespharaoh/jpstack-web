package wbs.platform.script.system.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.DeletedField;
import wbs.framework.entity.annotations.DescriptionField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.NameField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.MajorRecord;
import wbs.framework.record.Record;
import wbs.platform.script.language.model.ScriptLanguageRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MajorEntity
public
class SystemScriptRec
	implements MajorRecord<SystemScriptRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@CodeField
	String code;

	// details

	@NameField
	String name;

	@DescriptionField
	String description = "";

	@DeletedField
	Boolean deleted = false;

	// settings

	@ReferenceField (
		nullable = true)
	ScriptLanguageRec scriptLanguage;

	@SimpleField
	String text = "";

	@SimpleField
	Boolean standalone = false;

	@SimpleField
	Integer revision = 0;

	// compare to

	@Override
	public
	int compareTo (
			Record<SystemScriptRec> otherRecord) {

		SystemScriptRec other =
			(SystemScriptRec) otherRecord;

		return new CompareToBuilder ()
			.append (getCode (), other.getCode ())
			.toComparison ();

	}

}
