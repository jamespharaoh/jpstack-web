package wbs.platform.affiliate.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.DescriptionField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.TypeEntity;
import wbs.framework.record.Record;
import wbs.framework.record.TypeRecord;
import wbs.platform.object.core.model.ObjectTypeRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@TypeEntity
public
class AffiliateTypeRec
	implements TypeRecord<AffiliateTypeRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	ObjectTypeRec parentObjectType;

	@CodeField
	String code;

	// details

	@DescriptionField
	String description;

	// compare to

	@Override
	public
	int compareTo (
			Record<AffiliateTypeRec> otherRecord) {

		AffiliateTypeRec other =
			(AffiliateTypeRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getParentObjectType (),
				other.getParentObjectType ())

			.append (
				getCode (),
				other.getCode ())

			.toComparison ();

	}

}