package wbs.platform.priv.model;

import java.util.HashSet;
import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.CollectionField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.LinkField;
import wbs.framework.entity.annotations.MinorEntity;
import wbs.framework.entity.annotations.ParentIdField;
import wbs.framework.entity.annotations.ParentTypeField;
import wbs.framework.entity.annotations.TypeField;
import wbs.framework.record.MinorRecord;
import wbs.framework.record.Record;
import wbs.platform.group.model.GroupRec;
import wbs.platform.object.core.model.ObjectTypeRec;
import wbs.platform.user.model.UserPrivRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MinorEntity
public
class PrivRec
	implements MinorRecord<PrivRec> {

	@GeneratedIdField
	Integer id;

	@ParentTypeField
	ObjectTypeRec parentObjectType;

	@ParentIdField
	Integer parentObjectId;

	@CodeField
	String code;

	@TypeField
	PrivTypeRec privType;

	@CollectionField (
		table = "user_priv")
	Set<UserPrivRec> userPrivs =
		new HashSet<UserPrivRec> ();

	@LinkField (
		table = "group_priv")
	Set<GroupRec> groups =
		new HashSet<GroupRec> ();

	// compare to

	@Override
	public
	int compareTo (
			Record<PrivRec> otherRecord) {

		PrivRec other =
			(PrivRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getParentObjectType (),
				other.getParentObjectType ())

			.append (
				getParentObjectId (),
				other.getParentObjectId ())

			.append (
				getCode (),
				other.getCode ())

			.toComparison ();

	}


}
