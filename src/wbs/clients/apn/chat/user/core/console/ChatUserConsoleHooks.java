package wbs.clients.apn.chat.user.core.console;

import static wbs.framework.utils.etc.Misc.doNothing;

import java.util.Map;

import javax.inject.Inject;

import lombok.NonNull;

import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.core.model.ChatUserSearch;
import wbs.console.helper.AbstractConsoleHooks;
import wbs.console.request.ConsoleRequestContext;

public
class ChatUserConsoleHooks
	extends AbstractConsoleHooks<ChatUserRec> {

	// dependencies

	@Inject
	ConsoleRequestContext requestContext;

	// implementation

	@Override
	public
	void applySearchFilter (
			@NonNull Object searchObject) {

		if (searchObject instanceof Map) {

			doNothing ();

		} else if (searchObject instanceof ChatUserSearch) {

			ChatUserSearch search =
				(ChatUserSearch)
				searchObject;

			search

				.chatId (
					requestContext.stuffInt (
						"chatId"));

		} else {

			throw new RuntimeException ();

		}

	}

}
