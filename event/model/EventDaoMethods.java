package wbs.platform.event.model;

import java.util.List;

public
interface EventDaoMethods {

	List<Integer> searchIds (
			EventSearch eventSearch);

}
