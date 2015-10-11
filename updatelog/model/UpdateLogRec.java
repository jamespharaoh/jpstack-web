package wbs.platform.updatelog.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IndexField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class UpdateLogRec
	implements CommonRecord<UpdateLogRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@CodeField
	String code;

	@IndexField
	Integer ref;

	// state

	@SimpleField
	Integer version;

	// compare to

	@Override
	public
	int compareTo (
			Record<UpdateLogRec> otherRecord) {

		UpdateLogRec other =
			(UpdateLogRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getCode (),
				other.getCode ())

			.append (
				getRef (),
				other.getRef ())

			.toComparison ();

	}

}
