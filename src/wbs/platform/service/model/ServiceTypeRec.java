package wbs.platform.service.model;

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
class ServiceTypeRec
	implements MinorRecord<ServiceTypeRec> {

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
	String description;

	// children

	@CollectionField (
		key = "type_id")
	Set<ServiceRec> services;

	// compare to

	@Override
	public
	int compareTo (
			Record<ServiceTypeRec> otherRecord) {

		ServiceTypeRec other =
			(ServiceTypeRec) otherRecord;

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
	interface ServiceTypeObjectHelperMethods {

	}

	// dao methods

	public static
	interface ServiceTypeDaoMethods {

		List<ServiceTypeRec> findByParentObjectType (
				ObjectTypeRec parentObjectType);

	}

}
