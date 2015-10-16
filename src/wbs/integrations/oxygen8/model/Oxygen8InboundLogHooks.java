package wbs.integrations.oxygen8.model;

import java.util.List;

import javax.inject.Inject;

import wbs.framework.object.AbstractObjectHooks;

public
class Oxygen8InboundLogHooks
	extends AbstractObjectHooks<Oxygen8InboundLogRec> {

	// dependencies

	@Inject
	Oxygen8InboundLogDao oxygen8InboundLogDao;

	// implementation

	@Override
	public
	List<Integer> searchIds (
			Object search) {

		Oxygen8InboundLogSearch oxygen8InboundLogSearch =
			(Oxygen8InboundLogSearch) search;

		return oxygen8InboundLogDao.searchIds (
			oxygen8InboundLogSearch);

	}

}