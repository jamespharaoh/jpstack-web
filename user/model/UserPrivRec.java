package wbs.platform.user.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.EphemeralEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IdentityReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.EphemeralRecord;
import wbs.framework.record.Record;
import wbs.platform.priv.model.PrivRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@EphemeralEntity
public
class UserPrivRec
	implements EphemeralRecord<UserPrivRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@IdentityReferenceField
	UserRec user;

	@IdentityReferenceField
	PrivRec priv;

	// details

	@SimpleField
	Boolean can = false;

	@SimpleField
	Boolean canGrant = false;

	// compare to

	@Override
	public
	int compareTo (
			Record<UserPrivRec> otherRecord) {

		UserPrivRec other =
			(UserPrivRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getUser (),
				other.getUser ())

			.append (
				getPriv (),
				other.getPriv ())

			.toComparison ();

	}

}
