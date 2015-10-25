package wbs.platform.event.model;

import java.util.List;

import javax.inject.Inject;

import wbs.framework.object.AbstractObjectHooks;

public
class EventHooks
	extends AbstractObjectHooks<EventRec> {

	// dependencies

	@Inject
	EventDao eventDao;

	// implmentation

	@Override
	public
	List<Integer> searchIds (
			Object search) {

		EventSearch eventSearch =
			(EventSearch) search;

		return eventDao.searchIds (
			eventSearch);

	}

}
