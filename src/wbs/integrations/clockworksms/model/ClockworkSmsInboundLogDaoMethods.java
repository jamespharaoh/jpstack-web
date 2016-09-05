package wbs.integrations.clockworksms.model;

import java.util.List;

public
interface ClockworkSmsInboundLogDaoMethods {

	List <Long> searchIds (
			ClockworkSmsInboundLogSearch clockworkSmsInboundLogSearch);

}
