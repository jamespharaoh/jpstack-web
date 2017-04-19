package wbs.integrations.fonix.model;

import java.util.List;

import wbs.framework.logging.TaskLogger;

public
interface FonixInboundLogDaoMethods {

	List <Long> searchIds (
			TaskLogger parentTaskLogger,
			FonixInboundLogSearch fonixInboundLogSearch);

}
