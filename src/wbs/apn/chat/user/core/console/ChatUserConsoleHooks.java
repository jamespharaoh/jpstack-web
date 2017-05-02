package wbs.apn.chat.user.core.console;

import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;

import lombok.NonNull;

import wbs.console.helper.core.ConsoleHooks;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

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
	implements ConsoleHooks <ChatUserRec> {

	// singleton dependencies

	@SingletonDependency
	EventLogic eventLogic;

	@WeakSingletonDependency
	GazetteerEntryObjectHelper gazetteerEntryHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RandomLogic randomLogic;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// indirect dependencies

	// implementation

	@Override
	public
	void applySearchFilter (
			@NonNull Transaction parentTransaction,
			@NonNull Object searchObject) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"applySearchFilter");

		) {

			ChatUserSearch search =
				genericCastUnchecked (
					searchObject);

			search

				.chatId (
					optionalOrNull (
						requestContext.stuffInteger (
							"chatId")))

			;

		}

	}

	@Override
	public
	void beforeCreate (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec chatUser) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"beforeCreate");

		) {

			GazetteerEntryRec gazetteerEntry =
				gazetteerEntryHelper.findByCodeRequired (
					transaction,
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

}
