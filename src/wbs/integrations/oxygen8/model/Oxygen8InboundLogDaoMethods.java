package wbs.integrations.oxygen8.model;

import java.util.List;

public
interface Oxygen8InboundLogDaoMethods {

	List <Long> searchIds (
			Oxygen8InboundLogSearch oxygen8InboundLogSearch);

}