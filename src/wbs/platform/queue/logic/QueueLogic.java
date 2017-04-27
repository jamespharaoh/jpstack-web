package wbs.platform.queue.logic;

import java.util.List;

import lombok.NonNull;

import wbs.framework.entity.record.Record;
import wbs.framework.logging.TaskLogger;

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
			TaskLogger parentTaskLogger,
			QueueSubjectRec queueSubject,
			Record <?> refObject,
			String source,
			String details);

	QueueItemRec createQueueItem (
			TaskLogger parentTaskLogger,
			QueueRec queue,
			Record <?> subjectObject,
			Record <?> refObject,
			String source,
			String details);

	default
	QueueItemRec createQueueItem (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Record <?> queueParent,
			@NonNull String queueCode,
			@NonNull Record <?> subjectObject,
			@NonNull Record <?> refObject,
			@NonNull String source,
			@NonNull String details) {

		return createQueueItem (
			parentTaskLogger,
			findQueueByCodeRequired (
				queueParent,
				queueCode),
			subjectObject,
			refObject,
			source,
			details);

	}

	void cancelQueueItem (
			TaskLogger parentTaskLogger,
			QueueItemRec queueItem);

	void processQueueItem (
			TaskLogger parentTaskLogger,
			QueueItemRec queueItem,
			UserRec user);

	QueueRec findQueue (
			Record<?> parentObject,
			String code);

	QueueSubjectRec findOrCreateQueueSubject (
			TaskLogger parentTaskLogger,
			QueueRec queue,
			Record <?> object);

	List <QueueItemRec> getActiveQueueItems (
			QueueSubjectRec queueSubject);

	boolean sliceHasQueueActivity (
			SliceRec slice);

}
