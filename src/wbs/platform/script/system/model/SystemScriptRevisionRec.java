package wbs.platform.script.system.model;

import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IndexField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.platform.user.model.UserRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class SystemScriptRevisionRec
	implements CommonRecord<SystemScriptRevisionRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	SystemScriptRec systemScript;

	@IndexField
	Integer revision;

	// details

	@SimpleField
	Date timestamp = new Date ();

	@ReferenceField
	UserRec user;

	@SimpleField
	String diff;

	// compare to

	@Override
	public
	int compareTo (
			Record<SystemScriptRevisionRec> otherRecord) {

		SystemScriptRevisionRec other =
			(SystemScriptRevisionRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getSystemScript (),
				other.getSystemScript ())

			.append (
				other.getRevision (),
				getRevision ())

			.toComparison ();

	}

}
