package wbs.platform.object.core.model;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.object.ObjectTypeEntry;
import wbs.framework.record.MajorRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MajorEntity
public
class ObjectTypeRec
	implements
		MajorRecord<ObjectTypeRec>,
		ObjectTypeEntry {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@CodeField
	String code;

	// compare to

	@Override
	public
	int compareTo (
			Record<ObjectTypeRec> otherRecord) {

		ObjectTypeRec other =
			(ObjectTypeRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getCode (),
				other.getCode ())

			.toComparison ();

	}

	// dao methods

	public static
	interface ObjectTypeDaoMethods {

		ObjectTypeRec findById (
				int id);

		ObjectTypeRec findByCode (
				String code);

		List<ObjectTypeRec> findAll ();

	}

}
