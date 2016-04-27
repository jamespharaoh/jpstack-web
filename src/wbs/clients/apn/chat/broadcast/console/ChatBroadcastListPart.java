package wbs.clients.apn.chat.broadcast.console;

import static wbs.framework.utils.etc.Misc.joinWithSeparator;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import wbs.clients.apn.chat.broadcast.model.ChatBroadcastObjectHelper;
import wbs.clients.apn.chat.broadcast.model.ChatBroadcastRec;
import wbs.clients.apn.chat.core.logic.ChatMiscLogic;
import wbs.clients.apn.chat.core.model.ChatObjectHelper;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.object.ObjectManager;
import wbs.framework.utils.TimeFormatter;
import wbs.platform.currency.logic.CurrencyLogic;

@PrototypeComponent ("chatBroadcastListPart")
public
class ChatBroadcastListPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	ChatBroadcastObjectHelper chatBroadcastHelper;

	@Inject
	ChatObjectHelper chatHelper;

	@Inject
	ChatMiscLogic chatMiscLogic;

	@Inject
	CurrencyLogic currencyLogic;

	@Inject
	ObjectManager objectManager;

	@Inject
	TimeFormatter timeFormatter;

	// state

	ChatRec chat;
	List<ChatBroadcastRec> broadcasts;

	// implementation

	@Override
	public
	void prepare () {

		chat =
			chatHelper.find (
				requestContext.stuffInt ("chatId"));

		broadcasts =
			chatBroadcastHelper.findRecentWindow (
				chat,
				0,
				100);

	}

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th>User</th>\n",
			"<th>Timestamp</th>\n",
			"<th>Chat user</th>\n",
			"<th>Message</th>\n",
			"<th>Count</th>\n",
			"<th>Search</th>\n",
			"<th>Blocked</th>\n",
			"<th>Opt out</th>\n",
			"</tr>\n");

		for (
			ChatBroadcastRec chatBroadcast
				: broadcasts
		) {

			printFormat (
				"<tr>\n");

			printFormat (
				"<td>%h</td>\n",
				objectManager.objectPathMini (
					chatBroadcast.getCreatedUser ()));

			printFormat (
				"<td>%h</td>\n",
				timeFormatter.timestampTimezoneString (
					chatMiscLogic.timezone (
						chat),
					chatBroadcast.getCreatedTime ()));

			printFormat (
				"<td>%h</td>\n",
				objectManager.objectPathMini (
					chatBroadcast.getChatUser (),
					chat));

			printFormat (
				"<td>%h</td>\n",
				chatBroadcast.getText ().getText ());

			printFormat (
				"<td>%h</td>\n",
				+ chatBroadcast.getNumAccepted ()
				+ chatBroadcast.getNumSent ());

			printFormat (
				"<td>%h</td>\n",
				chatBroadcast.getSearch ()
					? joinWithSeparator (
						", ",
						getSearchParams (chatBroadcast))
					: "manual number selection");

			printFormat (
				"<td>%h</td>\n",
				chatBroadcast.getIncludeBlocked ()
					? "yes"
					: "no");

			printFormat (
				"<td>%h</td>\n",
				chatBroadcast.getIncludeOptedOut ()
					? "yes"
					: "no");

			printFormat (
				"</td>\n");

		}

		printFormat (
			"</table>\n");

	}

	List<String> getSearchParams (
			ChatBroadcastRec chatBroadcast) {

		List<String> searchParams =
			new ArrayList<String> ();

		if (chatBroadcast.getSearchLastActionFrom () != null
				&& chatBroadcast.getSearchLastActionTo () != null) {

			searchParams.add (
				stringFormat (
					"last action %s to %s",
					timeFormatter.timestampTimezoneString (
						chatMiscLogic.timezone (
							chat),
						chatBroadcast.getSearchLastActionFrom ()),
					timeFormatter.timestampTimezoneString (
						chatMiscLogic.timezone (
							chat),
						chatBroadcast.getSearchLastActionTo ())));

		} else if (chatBroadcast.getSearchLastActionFrom () != null) {

			searchParams.add (
				stringFormat (
					"last action from %s",
					timeFormatter.timestampTimezoneString (
						chatMiscLogic.timezone (
							chat),
						chatBroadcast.getSearchLastActionFrom ())));

		} else if (chatBroadcast.getSearchLastActionTo () != null) {

			searchParams.add (
				stringFormat (
					"last action to %s",
					timeFormatter.timestampTimezoneString (
						chatMiscLogic.timezone (
							chat),
						chatBroadcast.getSearchLastActionTo ())));

		}

		if (chatBroadcast.getSearchGender () != null) {

			searchParams.add (
				chatBroadcast.getSearchGender ().toString ());

		}

		if (chatBroadcast.getSearchOrient () != null) {

			searchParams.add (
				chatBroadcast.getSearchOrient ().toString ());

		}

		if (chatBroadcast.getSearchPicture () != null) {

			searchParams.add (
				chatBroadcast.getSearchPicture ()
					? "picture"
					: "no picture");

		}

		if (chatBroadcast.getSearchAdult () != null) {

			searchParams.add (
				chatBroadcast.getSearchAdult ()
					? "adult"
					: "non adult");

		}

		if (chatBroadcast.getSearchSpendMin () != null
				&& chatBroadcast.getSearchSpendMax () != null) {

			searchParams.add (
				stringFormat (
					"total spend %s to %s",
					currencyLogic.formatText (
						chatBroadcast.getChat ().getCurrency (),
						Long.valueOf(chatBroadcast.getSearchSpendMin ())),
					currencyLogic.formatText (
						chatBroadcast.getChat ().getCurrency (),
						Long.valueOf(chatBroadcast.getSearchSpendMax ()))));

		} else if (chatBroadcast.getSearchSpendMin () != null) {

			searchParams.add (
				stringFormat (
					"total spend at least %s",
					currencyLogic.formatText (
						chatBroadcast.getChat ().getCurrency (),
						Long.valueOf(chatBroadcast.getSearchSpendMin ()))));

		} else if (chatBroadcast.getSearchSpendMax () != null) {

			searchParams.add (
				stringFormat (
					"total spend at most %s",
					currencyLogic.formatText (
						chatBroadcast.getChat ().getCurrency (),
						Long.valueOf(chatBroadcast.getSearchSpendMax ()))));

		}

		return searchParams;

	}

}
