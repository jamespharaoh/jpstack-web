package wbs.integrations.oxygen8.model;

import java.util.List;

import wbs.integrations.oxygen8.model.Oxygen8InboundLogRec.Oxygen8InboundLogSearch;

public
interface Oxygen8InboundLogDaoMethods {

	List<Integer> searchIds (
			Oxygen8InboundLogSearch oxygen8InboundLogSearch);

}