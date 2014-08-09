package wbs.platform.scaffold.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import wbs.framework.entity.annotations.AssignedIdField;
import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.RootEntity;
import wbs.framework.record.MajorRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@RootEntity
public
class RootRec
	implements MajorRecord<RootRec> {

	// id

	@AssignedIdField
	Integer id;

	// identity

	@CodeField
	String code;

	// compare to

	@Override
	public
	int compareTo (
			Record<RootRec> otherRecord) {

		if (otherRecord != this)
			throw new RuntimeException ();

		return 0;

	}

}
