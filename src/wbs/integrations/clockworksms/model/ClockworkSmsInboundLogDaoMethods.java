package wbs.integrations.clockworksms.model;

import java.util.List;

import wbs.framework.logging.TaskLogger;

public
interface ClockworkSmsInboundLogDaoMethods {

	List <Long> searchIds (
			TaskLogger parentTaskLogger,
			ClockworkSmsInboundLogSearch clockworkSmsInboundLogSearch);

}
