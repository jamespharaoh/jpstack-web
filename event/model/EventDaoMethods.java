package wbs.platform.event.model;

import java.util.List;

import wbs.framework.logging.TaskLogger;

public
interface EventDaoMethods {

	List <Long> searchIds (
			TaskLogger parentTaskLogger,
			EventSearch eventSearch);

}
