package wbs.platform.queue.model;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CollectionField;
import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IdentitySimpleField;
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
class QueueSubjectRec
	implements CommonRecord<QueueSubjectRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	QueueRec queue;

	@IdentitySimpleField
	Integer objectId;

	// state

	@ReferenceField (
		nullable = true)
	UserRec preferredUser;

	// statistics

	@SimpleField
	Integer totalItems = 0;

	@SimpleField
	Integer activeItems = 0;

	// children

	@CollectionField (
		index = "index")
	List<QueueItemRec> queueItems;

	// utility methods

	public
	List<QueueItemRec> getActiveQueueItems () {

		return queueItems.subList (
			getTotalItems () - getActiveItems (),
			getTotalItems ());

	}

	// compare to

	@Override
	public
	int compareTo (
			Record<QueueSubjectRec> otherRecord) {

		QueueSubjectRec other =
			(QueueSubjectRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getQueue (),
				other.getQueue ())

			.append (
				getObjectId (),
				other.getObjectId ())

			.toComparison ();

	}

	// dao

	public static
	interface QueueSubjectDaoMethods {

		QueueSubjectRec find (
				QueueRec queue,
				Record<?> object);

		List<QueueSubjectRec> findActive ();

		List<QueueSubjectRec> findActive (
				QueueRec queue);

	}

}
