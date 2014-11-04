package wbs.platform.user.model;

import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class UserSessionRec
	implements CommonRecord<UserSessionRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	UserRec user;

	// TODO index?

	// details

	@SimpleField
	Date startTime;

	@SimpleField (
		nullable = true)
	Date endTime;

	// compare to

	@Override
	public
	int compareTo (
			Record<UserSessionRec> otherRecord) {

		UserSessionRec other =
			(UserSessionRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getUser (),
				other.getUser ())

			.append (
				other.getStartTime (),
				getStartTime ())

			.append (
				other.getId (),
				getId ())

			.toComparison ();

	}

}
