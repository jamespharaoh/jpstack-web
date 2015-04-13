package wbs.platform.queue.model;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.joda.time.Interval;

import wbs.framework.entity.annotations.CommonEntity;
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
class QueueItemRec
	implements CommonRecord<QueueItemRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	QueueSubjectRec queueSubject;

	@IndexField
	Integer index;

	// details

	@SimpleField
	String source;

	@SimpleField
	String details;

	@SimpleField
	Integer refObjectId;

	@ReferenceField (
		nullable = true)
	UserRec processedUser;

	@SimpleField (
		nullable = true)
	Boolean processedByPreferredUser;

	@SimpleField
	Integer priority = 0;

	@SimpleField
	Date createdTime;

	@SimpleField (
		nullable = true)
	Date pendingTime;

	@SimpleField (
		nullable = true)
	Date cancelledTime;

	@SimpleField (
		nullable = true)
	Date processedTime;

	// state

	@SimpleField
	QueueItemState state;

	@ReferenceField (
		nullable = true)
	QueueItemClaimRec queueItemClaim;

	// TODO this is legacy, fix the data and remove
	@ReferenceField
	QueueRec queue;

	// compare to

	@Override
	public
	int compareTo (
			Record<QueueItemRec> otherRecord) {

		QueueItemRec other =
			(QueueItemRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				other.getId (),
				getId ())

			.toComparison ();

	}

	// processed time comparator

	public final static
	Comparator<QueueItemRec> processedTimeComparator =
		new Comparator<QueueItemRec>() {

		@Override
		public
		int compare (
				QueueItemRec queueItemLeft,
				QueueItemRec queueItemRight) {

			return queueItemLeft.getProcessedTime ().compareTo (
				queueItemRight.getProcessedTime ());

		}

	};

	// object dao

	public static
	interface QueueItemDaoMethods {

		QueueItemRec findByIndex (
				QueueSubjectRec queueSubject,
				int index);

		List<QueueItemRec> findByCreatedTime (
				Interval createdTimeInterval);

		List<QueueItemRec> findByProcessedTime (
				Interval processedTimeInterval);

		List<QueueItemRec> findByProcessedTime (
				UserRec user,
				Interval processedTimeInterval);

	}

}
