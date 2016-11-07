package wbs.apn.chat.contact.console;

import com.google.common.collect.ImmutableList;

import wbs.apn.chat.contact.model.ChatUserInitiationLogSearch;
import wbs.apn.chat.contact.model.ChatUserInitiationLogRec;
import wbs.apn.chat.core.console.ChatConsoleHelper;
import wbs.apn.chat.core.model.ChatRec;
import wbs.console.helper.core.ConsoleHooks;
import wbs.console.priv.UserPrivChecker;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;

@SingletonComponent ("chatUserInitiationLogConsoleHooks")
public
class ChatUserInitiationLogConsoleHooks
	implements ConsoleHooks <ChatUserInitiationLogRec> {

	// singleton dependencies

	@SingletonDependency
	ChatConsoleHelper chatHelper;

	@SingletonDependency
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

		ImmutableList.Builder<Long> chatsBuilder =
			ImmutableList.builder ();

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
