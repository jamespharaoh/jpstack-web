package wbs.apn.chat.graphs.console;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.ChatUserSessionObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserSessionRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.request.ConsoleRequestContext;

@PrototypeComponent ("chatGraphsDailyUsersImageResponder")
public
class ChatGraphsDailyUsersImageResponder
	extends MonthlyHistoGraphImageResponder {

	@Inject
	ChatUserSessionObjectHelper chatUserSessionHelper;

	@Inject
	ConsoleRequestContext requestContext;

	public
	ChatGraphsDailyUsersImageResponder () {

		super (
			640,
			320,
			10);

	}

	@Override
	protected
	void prepareData (
			Date minTime,
			Date maxTime) {

		Calendar calendar =
			Calendar.getInstance ();

		Map<String,Object> searchMap =
			new LinkedHashMap<String,Object> ();

		searchMap.put (
			"startTimeAfter",
			minTime);

		searchMap.put (
			"startTimeBefore",
			maxTime);

		Integer chatAffiliateId =
			(Integer)
			requestContext.contextStuff ().get ("chatAffiliateId");

		if (chatAffiliateId != null) {

			searchMap.put (
				"chatAffiliateId",
				chatAffiliateId);

		}

		Collection<ChatUserSessionRec> chatUserSessions =
			chatUserSessionHelper.search (
				searchMap);

		List<Set<ChatUserRec>> chatUserSets =
			new ArrayList<Set<ChatUserRec>> ();

		for (int i = 0; i < values.size (); i ++) {

			chatUserSets.add (
				new HashSet<ChatUserRec> ());

		}

		for (ChatUserSessionRec chatUserSession
				: chatUserSessions) {

			calendar.setTime (
				chatUserSession.getStartTime ());

			int index =
				calendar.get (Calendar.DATE) - 1;

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
