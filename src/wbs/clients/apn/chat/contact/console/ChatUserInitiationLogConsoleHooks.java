package wbs.clients.apn.chat.contact.console;

import javax.inject.Inject;

import com.google.common.collect.ImmutableList;

import wbs.clients.apn.chat.contact.model.ChatUserInitiationLogRec;
import wbs.clients.apn.chat.contact.model.ChatUserInitiationLogSearch;
import wbs.clients.apn.chat.core.console.ChatConsoleHelper;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.console.helper.ConsoleHooks;
import wbs.console.priv.UserPrivChecker;
import wbs.framework.application.annotations.SingletonComponent;

@SingletonComponent ("chatUserInitiationLogConsoleHooks")
public
class ChatUserInitiationLogConsoleHooks
	implements ConsoleHooks<ChatUserInitiationLogRec> {

	// dependencies

	@Inject
	ChatConsoleHelper chatHelper;

	@Inject
	UserPrivChecker privChecker;

	// implementation

	@Override
	public
	void applySearchFilter (
			Object searchObject) {

		ChatUserInitiationLogSearch search =
			(ChatUserInitiationLogSearch)
			searchObject;

		search

			.filter (
				true);

		// chats

		ImmutableList.Builder<Integer> chatsBuilder =
			ImmutableList.<Integer>builder ();

		for (
			ChatRec chat
				: chatHelper.findAll ()
		) {

			 if (
			 	! privChecker.canRecursive (
			 		chat,
			 		"supervisor")
			 ) {
			 	continue;
			 }

			chatsBuilder.add (
				chat.getId ());

		}

		search

			.filterChatIds (
				chatsBuilder.build ());

	}

}
