package wbs.clients.apn.chat.user.core.model;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import wbs.framework.object.AbstractObjectHooks;

public
class ChatUserHooks
	extends AbstractObjectHooks<ChatUserRec> {

	// dependencies

	@Inject
	ChatUserDao chatUserDao;

	// implementation

	@Override
	public
	List<Integer> searchIds (
			Object search) {

		@SuppressWarnings ("unchecked")
		Map<String,Object> chatUserSearch =
			(Map<String,Object>) search;

		return chatUserDao.searchIds (
			chatUserSearch);

	}

	@Override
	public
	void beforeUpdate (
			ChatUserRec object) {

		System.out.println (
			"BEFORE UPDATE " + object);

	}

}