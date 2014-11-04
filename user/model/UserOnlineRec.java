package wbs.platform.user.model;

import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.EphemeralEntity;
import wbs.framework.entity.annotations.ForeignIdField;
import wbs.framework.entity.annotations.MasterField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.EphemeralRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@EphemeralEntity
public
class UserOnlineRec
	implements EphemeralRecord<UserOnlineRec> {

	// id

	@ForeignIdField (
		field = "user")
	Integer id;

	// identity

	@MasterField
	UserRec user;

	// state

	@SimpleField
	String sessionId;

	@SimpleField
	Date timestamp;

	@ReferenceField
	UserSessionRec userSession;

	// compare to

	@Override
	public
	int compareTo (
			Record<UserOnlineRec> otherRecord) {

		UserOnlineRec other =
			(UserOnlineRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getUser (),
				other.getUser ())

			.toComparison ();

	}

}
