package wbs.clients.apn.chat.user.core.console;

import static wbs.framework.utils.etc.Misc.doNothing;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.NonNull;

import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.core.model.ChatUserSearch;
import wbs.clients.apn.chat.user.core.model.ChatUserType;
import wbs.console.helper.ConsoleHooks;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.utils.RandomLogic;
import wbs.platform.event.logic.EventLogic;
import wbs.sms.gazetteer.model.GazetteerEntryObjectHelper;
import wbs.sms.gazetteer.model.GazetteerEntryRec;

@SingletonComponent ("chatUserConsoleHooks")
public
class ChatUserConsoleHooks
	implements ConsoleHooks<ChatUserRec> {

	// dependencies

	@Inject
	EventLogic eventLogic;

	@Inject
	RandomLogic randomLogic;

	@Inject
	ConsoleRequestContext requestContext;

	// indirect dependencies

	@Inject
	Provider<GazetteerEntryObjectHelper> gazetteerEntryHelperProvider;

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
						"chatId"))

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

		GazetteerEntryObjectHelper gazetteerEntryHelper =
			gazetteerEntryHelperProvider.get ();

		GazetteerEntryRec gazetteerEntry =
			gazetteerEntryHelper.findByCode (
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
