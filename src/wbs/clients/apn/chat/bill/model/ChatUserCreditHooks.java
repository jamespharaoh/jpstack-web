package wbs.clients.apn.chat.bill.model;

import java.util.List;

import javax.inject.Inject;

import lombok.NonNull;

import wbs.framework.object.AbstractObjectHooks;

public
class ChatUserCreditHooks
	extends AbstractObjectHooks<ChatUserCreditRec> {

	// dependencies

	@Inject
	ChatUserCreditDao chatUserCreditDao;

	// implementation

	@Override
	public
	List<Integer> searchIds (
			@NonNull Object searchObject) {

		ChatUserCreditSearch search =
			(ChatUserCreditSearch)
			searchObject;

		return chatUserCreditDao.searchIds (
			search);

	}

}