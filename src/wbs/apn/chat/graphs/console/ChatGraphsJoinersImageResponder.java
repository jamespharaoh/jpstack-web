package wbs.apn.chat.graphs.console;

import static wbs.utils.etc.OptionalUtils.optionalOrNull;

import java.util.List;

import lombok.NonNull;

import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.Interval;

import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.logging.TaskLogger;

import wbs.utils.time.TextualInterval;

import wbs.apn.chat.core.console.ChatConsoleHelper;
import wbs.apn.chat.core.logic.ChatMiscLogic;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.ChatUserSearch;

@PrototypeComponent ("chatGraphsJoinersImageResponder")
public
class ChatGraphsJoinersImageResponder
	extends MonthlyHistoGraphImageResponder {

	// dependencies

	@SingletonDependency
	ChatConsoleHelper chatHelper;

	@SingletonDependency
	ChatMiscLogic chatMiscLogic;

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@SingletonDependency
	Database database;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// state

	DateTimeZone timezone;

	// implementation

	public
	ChatGraphsJoinersImageResponder () {

		super (
			640,
			320,
			10);

	}

	@Override
	protected
	DateTimeZone timezone () {
		return timezone;
	}

	@Override
	protected
	void prepareData (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Instant minTime,
			@NonNull Instant maxTime) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"prepareData");

		ChatRec chat =
			chatHelper.findFromContextRequired ();

		List <Long> chatUserIds =
			chatUserHelper.searchIds (
				taskLogger,
				new ChatUserSearch ()

			.chatId (
				chat.getId ())

			.firstJoin (
				TextualInterval.forInterval (
					DateTimeZone.UTC,
					new Interval (
						minTime,
						maxTime)))

			.chatAffiliateId (
				optionalOrNull (
					requestContext.stuffInteger (
						"chatAffiliateId")))

		);

		timezone =
			chatMiscLogic.timezone (
				chat);

		for (
			Long chatUserId
				: chatUserIds
		) {

			ChatUserRec chatUser =
				chatUserHelper.findRequired (
					chatUserId);

			int index =
				+ chatUser.getFirstJoin ()
					.toDateTime (timezone)
					.getDayOfMonth ()
				- 1;

			values.set (
				index,
				values.get (index) + 1);

		}

	}

}
