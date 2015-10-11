package wbs.applications.imchat.model;

import java.util.List;

import javax.inject.Inject;

import wbs.framework.object.AbstractObjectHooks;

public
class ImChatCustomerHooks
	extends AbstractObjectHooks<ImChatCustomerRec> {

	// dependencies

	@Inject
	ImChatCustomerDao imChatCustomerDao;

	// implementation

	@Override
	public
	List<Integer> searchIds (
			Object search) {

		ImChatCustomerSearch imChatCustomerSearch =
			(ImChatCustomerSearch) search;

		return imChatCustomerDao.searchIds (
			imChatCustomerSearch);

	}

}