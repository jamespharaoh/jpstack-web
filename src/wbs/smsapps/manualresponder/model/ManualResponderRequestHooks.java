package wbs.smsapps.manualresponder.model;

import java.util.List;

import javax.inject.Inject;

import wbs.framework.object.AbstractObjectHooks;

public
class ManualResponderRequestHooks
	extends AbstractObjectHooks<ManualResponderRequestRec> {

	@Inject
	ManualResponderRequestDao manualResponderRequestDao;

	@Override
	public
	List<Integer> searchIds (
			Object searchObject) {

		ManualResponderRequestSearch search =
			(ManualResponderRequestSearch) searchObject;

		return manualResponderRequestDao.searchIds (
			search);

	}

}