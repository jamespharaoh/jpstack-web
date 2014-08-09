package wbs.apn.chat.graphs.console;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import wbs.apn.chat.core.model.ChatObjectHelper;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.context.ConsoleContextStuff;
import wbs.platform.console.request.ConsoleRequestContext;

@PrototypeComponent ("chatGraphsJoinersImageResponder")
public
class ChatGraphsJoinersImageResponder
	extends MonthlyHistoGraphImageResponder {

	@Inject
	ChatObjectHelper chatHelper;

	@Inject
	ChatUserConsoleHelper chatUserHelper;

	@Inject
	ConsoleRequestContext requestContext;

	public ChatGraphsJoinersImageResponder () {

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

		ConsoleContextStuff contextStuff =
			requestContext.contextStuff ();

		Integer chatId = (Integer)
			contextStuff.get ("chatId");

		ChatRec chat =
			chatHelper.find (
				chatId);

		Map<String,Object> searchMap =
			new LinkedHashMap<String,Object> ();

		searchMap.put ("chatId", chat.getId ());
		searchMap.put ("firstJoinAfter", minTime);
		searchMap.put ("firstJoinBefore", maxTime);

		Integer chatAffiliateId = (Integer)
			contextStuff.get ("chatAffiliateId");

		if (chatAffiliateId != null)
			searchMap.put ("chatAffiliateId", chatAffiliateId);

		List<Integer> chatUserIds =
			chatUserHelper.searchIds (
				searchMap);

		Calendar cal = new GregorianCalendar ();

		for (Integer chatUserId : chatUserIds) {

			ChatUserRec chatUser =
				chatUserHelper.find (
					chatUserId);

			cal.setTime (chatUser.getFirstJoin ());

			int index = cal.get (Calendar.DATE) - 1;

			values.set (index, values.get (index) + 1);

		}

	}

}
