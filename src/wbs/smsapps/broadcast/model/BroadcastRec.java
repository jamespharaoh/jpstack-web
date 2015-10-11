package wbs.smsapps.broadcast.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.joda.time.Instant;

import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.DescriptionField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IndexField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.platform.user.model.UserRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class BroadcastRec
	implements CommonRecord<BroadcastRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	BroadcastConfigRec broadcastConfig;

	@IndexField (
		counter = "numTotal")
	Integer index;

	// details

	@DescriptionField
	String description;

	// state

	@SimpleField
	BroadcastState state =
		BroadcastState.unsent;

	// message details

	@SimpleField
	String messageOriginator = "";

	@SimpleField
	String messageText = "";

	// various timestamps

	@SimpleField
	Instant createdTime;

	@SimpleField (
		nullable = true)
	Instant scheduledTime;

	@SimpleField (
		nullable = true)
	Instant sentTime;

	@SimpleField (
		nullable = true)
	Instant cancelledTime;

	// various involved users

	@ReferenceField
	UserRec createdUser;

	@ReferenceField (
		nullable = true)
	UserRec sentUser;

	@ReferenceField (
		nullable = true)
	UserRec cancelledUser;

	// statistics about numbers

	@SimpleField
	Integer numRemoved = 0;

	@SimpleField
	Integer numAccepted = 0;

	@SimpleField
	Integer numRejected = 0;

	@SimpleField
	Integer numSent = 0;

	public
	Integer getNumTotal () {
		return getNumAccepted () + getNumSent ();
	}

	// compare to

	@Override
	public
	int compareTo (
			Record<BroadcastRec> otherRecord) {

		BroadcastRec other =
			(BroadcastRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				other.getCreatedTime (),
				getCreatedTime ())

			.toComparison ();

	}

}
