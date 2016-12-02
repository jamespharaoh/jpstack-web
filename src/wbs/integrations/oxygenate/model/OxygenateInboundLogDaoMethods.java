package wbs.integrations.oxygenate.model;

import java.util.List;

public
interface OxygenateInboundLogDaoMethods {

	List <Long> searchIds (
			OxygenateInboundLogSearch oxygenateInboundLogSearch);

}