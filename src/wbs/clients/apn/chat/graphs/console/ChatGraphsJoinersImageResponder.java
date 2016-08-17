package wbs.clients.apn.chat.graphs.console;

import java.util.List;

import javax.inject.Inject;

import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.Interval;

import wbs.clients.apn.chat.core.logic.ChatMiscLogic;
import wbs.clients.apn.chat.core.model.ChatObjectHelper;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.core.model.ChatUserSearch;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.utils.TextualInterval;

@PrototypeComponent ("chatGraphsJoinersImageResponder")
public
class ChatGraphsJoinersImageResponder
	extends MonthlyHistoGraphImageResponder {

	// dependencies

	@Inject
	ChatObjectHelper chatHelper;

	@Inject
	ChatMiscLogic chatMiscLogic;

	@Inject
	ChatUserConsoleHelper chatUserHelper;

	@Inject
	Database database;

	@Inject
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
			Instant minTime,
			Instant maxTime) {

		ChatRec chat =
			chatHelper.findRequired (
				requestContext.stuffInteger (
					"chatId"));

		List<Long> chatUserIds =
			chatUserHelper.searchIds (
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
				requestContext.stuffInteger (
					"chatAffiliateId"))

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
