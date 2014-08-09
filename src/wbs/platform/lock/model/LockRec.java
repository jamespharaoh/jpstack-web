package wbs.platform.lock.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.AssignedIdField;
import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class LockRec
	implements CommonRecord<LockRec> {

	@AssignedIdField
	Integer id;

	@Override
	public
	int compareTo (
			Record<LockRec> otherRecord) {

		LockRec other =
			(LockRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getId (),
				other.getId ())

			.toComparison ();

	}

}
