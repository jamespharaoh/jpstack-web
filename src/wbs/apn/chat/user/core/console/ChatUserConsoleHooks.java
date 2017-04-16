package wbs.apn.chat.user.core.console;

import static wbs.utils.etc.Misc.doNothing;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;

import java.util.Map;

import lombok.NonNull;

import wbs.console.helper.core.ConsoleHooks;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.logging.TaskLogger;

import wbs.platform.event.logic.EventLogic;

import wbs.sms.gazetteer.model.GazetteerEntryObjectHelper;
import wbs.sms.gazetteer.model.GazetteerEntryRec;

import wbs.utils.random.RandomLogic;

import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.ChatUserSearch;
import wbs.apn.chat.user.core.model.ChatUserType;

@SingletonComponent ("chatUserConsoleHooks")
public
class ChatUserConsoleHooks
	implements ConsoleHooks<ChatUserRec> {

	// singleton dependencies

	@SingletonDependency
	EventLogic eventLogic;

	@WeakSingletonDependency
	GazetteerEntryObjectHelper gazetteerEntryHelper;

	@SingletonDependency
	RandomLogic randomLogic;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// indirect dependencies

	// implementation

	@Override
	public
	void applySearchFilter (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Object searchObject) {

		if (searchObject instanceof Map) {

			doNothing ();

		} else if (searchObject instanceof ChatUserSearch) {

			ChatUserSearch search =
				(ChatUserSearch)
				searchObject;

			search

				.chatId (
					optionalOrNull (
						requestContext.stuffInteger (
							"chatId")))

				.deleted (
					false);

		} else {

			throw new RuntimeException ();

		}

	}

	@Override
	public
	void beforeCreate (
			@NonNull ChatUserRec chatUser) {

		GazetteerEntryRec gazetteerEntry =
			gazetteerEntryHelper.findByCodeRequired (
				chatUser.getChat ().getGazetteer (),
				chatUser.getLocationPlace ());

		chatUser

			.setCode (
				randomLogic.generateNumericNoZero (6))

			.setType (
				ChatUserType.monitor)

			.setLocationPlaceLongLat (
				gazetteerEntry.getLongLat ())

			.setLocationLongLat (
				gazetteerEntry.getLongLat ());

	}

}
