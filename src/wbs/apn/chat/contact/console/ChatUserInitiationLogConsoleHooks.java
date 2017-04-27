package wbs.apn.chat.contact.console;

import static wbs.utils.etc.TypeUtils.genericCastUnchecked;

import com.google.common.collect.ImmutableList;

import lombok.NonNull;

import wbs.console.helper.core.ConsoleHooks;
import wbs.console.priv.UserPrivChecker;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.apn.chat.contact.model.ChatUserInitiationLogRec;
import wbs.apn.chat.contact.model.ChatUserInitiationLogSearch;
import wbs.apn.chat.core.console.ChatConsoleHelper;
import wbs.apn.chat.core.model.ChatRec;

@SingletonComponent ("chatUserInitiationLogConsoleHooks")
public
class ChatUserInitiationLogConsoleHooks
	implements ConsoleHooks <ChatUserInitiationLogRec> {

	// singleton dependencies

	@SingletonDependency
	ChatConsoleHelper chatHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	UserPrivChecker privChecker;

	// implementation

	@Override
	public
	void applySearchFilter (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Object searchObject) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"applySearchFilter");

		) {

			ChatUserInitiationLogSearch search =
				genericCastUnchecked (
					searchObject);

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
						taskLogger,
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

}
