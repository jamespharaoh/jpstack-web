
package wbs.platform.user.model;

import java.util.HashSet;
import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.CollectionField;
import wbs.framework.entity.annotations.DeletedField;
import wbs.framework.entity.annotations.DescriptionField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.LinkField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.entity.annotations.SlaveField;
import wbs.framework.record.MajorRecord;
import wbs.framework.record.Record;
import wbs.platform.group.model.GroupRec;
import wbs.platform.scaffold.model.SliceRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MajorEntity
public
class UserRec
	implements MajorRecord<UserRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	SliceRec slice;

	@CodeField
	String username;

	// details

	@DescriptionField
	String details = "";

	@DeletedField
	Boolean deleted = false;

	// settings

	@SimpleField
	String fullname = "";

	@SimpleField (
		nullable = true)
	String password;

	@SimpleField
	Boolean active = false;

	@SlaveField
	UserOnlineRec userOnline;

	// children

	@CollectionField
	Set<UserPrivRec> userPrivs =
		new HashSet<UserPrivRec> ();

	@LinkField (
		table = "user_group")
	Set<GroupRec> groups =
		new HashSet<GroupRec> ();

	@CollectionField
	Set<UserSessionRec> userSessions =
		new HashSet<UserSessionRec> ();

	// compare to

	@Override
	public
	int compareTo (
			Record<UserRec> otherRecord) {

		UserRec other =
			(UserRec) otherRecord;

		return new CompareToBuilder ()
			.append (getSlice (), other.getSlice ())
			.append (getUsername (), other.getUsername ())
			.toComparison ();

	}

}
