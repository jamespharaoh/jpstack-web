package wbs.apn.chat.graphs.console;

import static wbs.utils.etc.OptionalUtils.optionalIsPresent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;

import org.joda.time.DateTimeZone;
import org.joda.time.Instant;

import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;

import wbs.apn.chat.core.console.ChatConsoleHelper;
import wbs.apn.chat.core.logic.ChatMiscLogic;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.console.ChatUserSessionConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.ChatUserSessionRec;

@PrototypeComponent ("chatGraphsDailyUsersImageResponder")
public
class ChatGraphsDailyUsersImageResponder
	extends MonthlyHistoGraphImageResponder {

	// dependencies

	@SingletonDependency
	ChatConsoleHelper chatHelper;

	@SingletonDependency
	ChatMiscLogic chatMiscLogic;

	@SingletonDependency
	ChatUserSessionConsoleHelper chatUserSessionHelper;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// state

	DateTimeZone timezone;

	// implementation

	public
	ChatGraphsDailyUsersImageResponder () {

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

		Map <String, Object> searchMap =
			new LinkedHashMap<> ();

		searchMap.put (
			"startTimeAfter",
			minTime);

		searchMap.put (
			"startTimeBefore",
			maxTime);

		ChatRec chat =
			chatHelper.findFromContextRequired ();

		searchMap.put (
			"chatId",
			chat.getId ());

		Optional <Long> chatAffiliateIdOptional =
			requestContext.stuffInteger (
				"chatAffiliateId");

		if (
			optionalIsPresent (
				chatAffiliateIdOptional)
		) {

			searchMap.put (
				"chatAffiliateId",
				chatAffiliateIdOptional.get ());

		}

		Collection <ChatUserSessionRec> chatUserSessions =
			chatUserSessionHelper.search (
				searchMap);

		List<Set<ChatUserRec>> chatUserSets =
			new ArrayList<Set<ChatUserRec>> ();

		for (int i = 0; i < values.size (); i ++) {

			chatUserSets.add (
				new HashSet<ChatUserRec> ());

		}

		timezone =
			chatMiscLogic.timezone (
				chat);

		for (ChatUserSessionRec chatUserSession
				: chatUserSessions) {

			int index =
				+ chatUserSession.getStartTime ()
					.toDateTime (
						timezone)
					.getDayOfMonth ()
				- 1;

			chatUserSets.get (index).add (
				chatUserSession.getChatUser ());

		}

		values =
			new ArrayList<Integer> ();

		for (Set<ChatUserRec> chatUserSet
				: chatUserSets) {

			values.add (
				chatUserSet.size ());

		}

	}

}
