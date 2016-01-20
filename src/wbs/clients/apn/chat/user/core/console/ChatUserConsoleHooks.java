package wbs.clients.apn.chat.user.core.console;

import static wbs.framework.utils.etc.Misc.doNothing;

import java.util.Map;

import javax.inject.Inject;

import lombok.NonNull;

import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.core.model.ChatUserSearch;
import wbs.console.helper.ConsoleHooks;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.SingletonComponent;

@SingletonComponent ("chatUserConsoleHooks")
public
class ChatUserConsoleHooks
	implements ConsoleHooks<ChatUserRec> {

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
