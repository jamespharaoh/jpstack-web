package wbs.platform.queue.model;

import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
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
class QueueItemClaimRec
	implements CommonRecord<QueueItemClaimRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	QueueItemRec queueItem;

	// TODO?

	// details

	@ReferenceField
	UserRec user;

	@ReferenceField (
		nullable = true)
	UserRec unclaimUser;

	@SimpleField
	Date startTime;

	@SimpleField (
		nullable = true)
	Date endTime;

	// state

	@SimpleField
	QueueItemClaimStatus status;

	// compare to

	@Override
	public
	int compareTo (
			Record<QueueItemClaimRec> otherRecord) {

		QueueItemClaimRec other =
			(QueueItemClaimRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				other.getQueueItem (),
				getQueueItem ())

			.append (
				other.getStartTime (),
				getStartTime ())

			.append (
				other.getId (),
				getId ())

			.toComparison ();

	}

}
