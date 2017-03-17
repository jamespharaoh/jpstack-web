package wbs.platform.queue.logic;

import java.util.List;

import lombok.NonNull;

import wbs.framework.entity.record.Record;

import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.queue.model.QueueRec;
import wbs.platform.queue.model.QueueSubjectRec;
import wbs.platform.scaffold.model.SliceRec;
import wbs.platform.user.model.UserRec;

public
interface QueueLogic {

	QueueRec findQueueByCodeRequired (
			Record <?> queueParent,
			String queueCode);

	QueueItemRec createQueueItem (
			QueueSubjectRec queueSubject,
			Record <?> refObject,
			String source,
			String details);

	QueueItemRec createQueueItem (
			QueueRec queue,
			Record <?> subjectObject,
			Record <?> refObject,
			String source,
			String details);

	default
	QueueItemRec createQueueItem (
			@NonNull Record <?> queueParent,
			@NonNull String queueCode,
			@NonNull Record <?> subjectObject,
			@NonNull Record <?> refObject,
			@NonNull String source,
			@NonNull String details) {

		return createQueueItem (
			findQueueByCodeRequired (
				queueParent,
				queueCode),
			subjectObject,
			refObject,
			source,
			details);

	}

	void cancelQueueItem (
			QueueItemRec queueItem);

	void processQueueItem (
			QueueItemRec queueItem,
			UserRec user);

	QueueRec findQueue (
			Record<?> parentObject,
			String code);

	QueueSubjectRec findOrCreateQueueSubject (
			QueueRec queue,
			Record<?> object);

	List<QueueItemRec> getActiveQueueItems (
			QueueSubjectRec queueSubject);

	boolean sliceHasQueueActivity (
			SliceRec slice);

}
