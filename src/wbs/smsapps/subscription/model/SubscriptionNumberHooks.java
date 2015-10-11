package wbs.smsapps.subscription.model;

import java.util.List;

import javax.inject.Inject;

import wbs.framework.object.AbstractObjectHooks;

public
class SubscriptionNumberHooks
	extends AbstractObjectHooks<SubscriptionNumberRec> {

	@Inject
	SubscriptionNumberDao subscriptionNumberDao;

	@Override
	public
	List<Integer> searchIds (
			Object searchObject) {

		SubscriptionNumberSearch search =
			(SubscriptionNumberSearch) searchObject;

		return subscriptionNumberDao.searchIds (
			search);

	}

}