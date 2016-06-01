package wbs.clients.apn.chat.affiliate.console;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;

import com.google.common.collect.ImmutableMap;

import wbs.clients.apn.chat.affiliate.model.ChatAffiliateRec;
import wbs.clients.apn.chat.core.model.ChatObjectHelper;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.console.part.AbstractPagePart;
import wbs.console.priv.UserPrivChecker;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.IntervalFormatter;

@PrototypeComponent ("chatAffiliateComparePart")
public
class ChatAffiliateComparePart
	extends AbstractPagePart {

	// dependencies

	@Inject
	ChatObjectHelper chatHelper;

	@Inject
	ChatUserConsoleHelper chatUserHelper;

	@Inject
	IntervalFormatter intervalFormatter;

	@Inject
	UserPrivChecker privChecker;

	// state

	String timePeriodString;
	List<ChatAffiliateWithNewUserCount> chatAffiliateWithNewUserCounts;

	// implementation

	@Override
	public
	void prepare () {

		// check units

		timePeriodString =
			requestContext.parameterOrDefault (
				"timePeriod",
				"7 days");

		Integer timePeriodSeconds =
			intervalFormatter.parseIntervalStringSecondsRequired (
				timePeriodString);

		if (timePeriodSeconds == null) {

			requestContext.addError (
				"Invalid time period");

			return;

		}

		// get objects

		ChatRec chat =
			chatHelper.find (
				requestContext.stuffInt ("chatId"));

		// work out first join time

		DateTimeZone timeZone =
			DateTimeZone.forID (
				chat.getTimezone ());

		Instant firstJoinAfter =
			DateTime
				.now (
					timeZone)
				.minusSeconds (
					timePeriodSeconds)
				.toInstant ();

		// get all relevant users

		List<Integer> newUserIds =
			chatUserHelper.searchIds (
				ImmutableMap.<String,Object>builder ()

			.put (
				"chatId",
				chat.getId ())

			.put (
				"firstJoinAfter",
				firstJoinAfter)

			.build ());

		// count them grouping by affiliate

		Map<Integer,ChatAffiliateWithNewUserCount> map =
			new HashMap<Integer,ChatAffiliateWithNewUserCount> ();

		for (Integer chatUserId
				: newUserIds) {

			ChatUserRec chatUser =
				chatUserHelper.find (
					chatUserId);

			Integer chatAffiliateId =
				chatUser.getChatAffiliate () != null ?
					chatUser.getChatAffiliate ().getId () : null;

			ChatAffiliateWithNewUserCount chatAffiliateWithNewUserCount =
				map.get (chatAffiliateId);

			if (chatAffiliateWithNewUserCount == null) {

				map.put (
					chatAffiliateId,
					chatAffiliateWithNewUserCount =
						new ChatAffiliateWithNewUserCount ()
							.chatAffiliate (chatUser.getChatAffiliate ()));

			}

			chatAffiliateWithNewUserCount.newUsers ++;

		}

		// now select and sort the ones we are allowed to see

		chatAffiliateWithNewUserCounts =
			new ArrayList<ChatAffiliateWithNewUserCount> ();

		if (privChecker.canRecursive (chat, "stats")) {

			chatAffiliateWithNewUserCounts.addAll (
				map.values ());

			for (
				ChatAffiliateWithNewUserCount chatAffiliateWithNewUserCount
					: map.values ()
			) {

				ChatAffiliateRec chatAffiliate =
					chatAffiliateWithNewUserCount.chatAffiliate;

				if (! privChecker.canRecursive (chatAffiliate, "stats"))
					continue;

				chatAffiliateWithNewUserCounts.add (
					chatAffiliateWithNewUserCount);

			}

		}

		Collections.sort (
			chatAffiliateWithNewUserCounts);

	}

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<form",
			" method=\"get\"",
			" action=\"%h\"",
			requestContext.resolveLocalUrl (
				"/chatAffiliate.compare"),
			">\n");

		printFormat (
			"<p>Time period<br>\n",

			"<input",
			" type=\"text\"",
			" name=\"timePeriod\"",
			" value=\"%h\"",
			timePeriodString,
			"\">",

			"<input",
			" type=\"submit\"",
			" value=\"ok\"></p>\n");

		printFormat (
			"</form>\n");

		if (chatAffiliateWithNewUserCounts == null)
			return;

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th>Scheme</th>\n",
			"<th>Affiliate</th>\n",
			"<th>Description</th>\n",
			"<th>New users</th>\n",
			"</tr>");

		for (ChatAffiliateWithNewUserCount chatAffiliateWithNewUserCount
				: chatAffiliateWithNewUserCounts) {

			ChatAffiliateRec chatAffiliate =
				chatAffiliateWithNewUserCount.chatAffiliate;

			printFormat (
				"<tr>\n",

				"<td>%h</td>\n",
				chatAffiliate != null
					? chatAffiliate.getChatScheme ().getCode ()
					: "(no affiliate)",

				"<td>%h</td>\n",
				chatAffiliate != null
					? chatAffiliate.getCode ()
					: "(no affiliate)",

				"<td>%h</td>\n",
				chatAffiliate != null
					? chatAffiliate.getDescription ()
					: "-",

				"<td>%h</td>\n",
				chatAffiliateWithNewUserCount.newUsers,

				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

}
