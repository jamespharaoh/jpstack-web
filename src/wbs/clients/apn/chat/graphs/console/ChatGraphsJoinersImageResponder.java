package wbs.clients.apn.chat.graphs.console;

import static wbs.framework.utils.etc.Misc.dateToInstant;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.joda.time.DateTimeZone;
import org.joda.time.Instant;

import wbs.clients.apn.chat.core.logic.ChatMiscLogic;
import wbs.clients.apn.chat.core.model.ChatObjectHelper;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.console.context.ConsoleContextStuff;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;

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

		ConsoleContextStuff contextStuff =
			requestContext.contextStuff ();

		Integer chatId =
			(Integer)
			contextStuff.get ("chatId");

		ChatRec chat =
			chatHelper.find (
				chatId);

		Map<String,Object> searchMap =
			new LinkedHashMap<String,Object> ();

		searchMap.put (
			"chatId",
			chat.getId ());

		searchMap.put (
			"firstJoinAfter",
			minTime);

		searchMap.put (
			"firstJoinBefore",
			maxTime);

		searchMap.put (
			"chatId",
			chat.getId ());

		Integer chatAffiliateId =
			(Integer)
			contextStuff.get ("chatAffiliateId");

		if (chatAffiliateId != null) {

			searchMap.put (
				"chatAffiliateId",
				chatAffiliateId);

		}

		List<Integer> chatUserIds =
			chatUserHelper.searchIds (
				searchMap);

		timezone =
			chatMiscLogic.timezone (
				chat);

		for (Integer chatUserId
				: chatUserIds) {

			ChatUserRec chatUser =
				chatUserHelper.find (
					chatUserId);

			int index =
				+ dateToInstant (chatUser.getFirstJoin ())
					.toDateTime (timezone)
					.getDayOfMonth ()
				- 1;

			values.set (
				index,
				values.get (index) + 1);

		}

	}

}
