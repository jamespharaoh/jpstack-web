package wbs.platform.queue.model;

import java.util.List;

import javax.inject.Inject;

import lombok.NonNull;

import wbs.framework.object.AbstractObjectHooks;

public
class QueueItemHooks
	extends AbstractObjectHooks<QueueItemRec> {

	// dependencies

	@Inject
	QueueItemDao queueItemDao;

	// implementation

	@Override
	public
	List<Integer> searchIds (
			@NonNull Object search) {

		QueueItemSearch queueItemSearch =
			(QueueItemSearch) search;

		return queueItemDao.searchIds (
			queueItemSearch);

	}

}
