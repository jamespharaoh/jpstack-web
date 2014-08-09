package wbs.platform.queue.model;

import java.util.List;
import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.CollectionField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.entity.annotations.TypeEntity;
import wbs.framework.record.MinorRecord;
import wbs.framework.record.Record;
import wbs.platform.object.core.model.ObjectTypeRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@TypeEntity
public
class QueueTypeRec
	implements MinorRecord<QueueTypeRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	ObjectTypeRec parentObjectType;

	@CodeField
	String code;

	// details

	@SimpleField
	String description = "";

	@ReferenceField
	ObjectTypeRec subjectObjectType;

	@ReferenceField
	ObjectTypeRec refObjectType;

	// children

	@CollectionField (
		key = "type_id")
	Set<QueueRec> queues;

	// compare to

	@Override
	public
	int compareTo (
			Record<QueueTypeRec> otherRecord) {

		QueueTypeRec other =
			(QueueTypeRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getParentObjectType (),
				other.getParentObjectType ())

			.append (
				getCode (),
				other.getCode ())

			.toComparison ();

	}

	// object helper methods

	public static
	interface QueueTypeObjectHelperMethods {

	}

	// dao methods

	public static
	interface QueueTypeDaoMethods {

		List<QueueTypeRec> findByParentObjectType (
				ObjectTypeRec parentType);

	}

}
