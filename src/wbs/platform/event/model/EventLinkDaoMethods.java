package wbs.platform.event.model;

import java.util.List;

public
interface EventLinkDaoMethods {

	List<EventLinkRec> findByTypeAndRef (
			long typeId,
			long refId);

}