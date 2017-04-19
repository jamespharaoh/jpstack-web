package wbs.integrations.oxygenate.model;

import java.util.List;

import wbs.framework.logging.TaskLogger;

public
interface OxygenateInboundLogDaoMethods {

	List <Long> searchIds (
			TaskLogger parentTaskLogger,
			OxygenateInboundLogSearch oxygenateInboundLogSearch);

}