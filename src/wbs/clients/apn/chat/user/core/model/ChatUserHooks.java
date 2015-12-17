package wbs.clients.apn.chat.user.core.model;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import lombok.NonNull;

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
			@NonNull Object searchObject) {

		if (searchObject instanceof Map) {

			@SuppressWarnings ("unchecked")
			Map<String,Object> searchMap =
				(Map<String,Object>) searchObject;

			return chatUserDao.searchIds (
				searchMap);

		} else if (searchObject instanceof ChatUserSearch) {

			ChatUserSearch search =
				(ChatUserSearch)
				searchObject;

			return chatUserDao.searchIds (
				search);

		} else {

			throw new RuntimeException ();

		}

	}

	@Override
	public
	void beforeUpdate (
			ChatUserRec object) {

		System.out.println (
			"BEFORE UPDATE " + object);

	}

}