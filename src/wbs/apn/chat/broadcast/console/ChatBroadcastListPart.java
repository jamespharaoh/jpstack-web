package wbs.apn.chat.broadcast.console;

import static wbs.framework.utils.etc.Misc.dateToInstant;
import static wbs.framework.utils.etc.Misc.joinWithSeparator;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import wbs.apn.chat.broadcast.model.ChatBroadcastObjectHelper;
import wbs.apn.chat.broadcast.model.ChatBroadcastRec;
import wbs.apn.chat.core.logic.ChatMiscLogic;
import wbs.apn.chat.core.model.ChatObjectHelper;
import wbs.apn.chat.core.model.ChatRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.object.ObjectManager;
import wbs.platform.console.misc.TimeFormatter;
import wbs.platform.console.part.AbstractPagePart;
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
	void goBodyStuff () {

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

		for (ChatBroadcastRec chatBroadcast
				: broadcasts) {

			printFormat (
				"<tr>\n",

				"<td>%h</td>\n",
				objectManager.objectPath (
					chatBroadcast.getUser (),
					null,
					true),

				"<td>%h</td>\n",
				timeFormatter.instantToTimestampString (
					chatMiscLogic.timezone (
						chat),
					dateToInstant (
						chatBroadcast.getTimestamp ())),

				"<td>%h</td>\n",
				objectManager.objectPath (
					chatBroadcast.getChatUser (),
					chat,
					true),

				"<td>%h</td>\n",
				chatBroadcast.getText ().getText (),

				"<td>%h</td>\n",
				chatBroadcast.getNumberCount (),

				"<td>%h</td>\n",
				chatBroadcast.getSearch ()
					? joinWithSeparator (
						", ",
						getSearchParams (chatBroadcast))
					: "manual number selection",

				"<td>%h</td>\n",
				chatBroadcast.getIncludeBlocked ()
					? "yes"
					: "no",

				"<td>%h</td>\n",
				chatBroadcast.getIncludeOptedOut ()
					? "yes"
					: "no",

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
					timeFormatter.instantToTimestampString (
						chatMiscLogic.timezone (
							chat),
						dateToInstant (
							chatBroadcast.getSearchLastActionFrom ())),
					timeFormatter.instantToTimestampString (
						chatMiscLogic.timezone (
							chat),
						dateToInstant (
							chatBroadcast.getSearchLastActionTo ()))));

		} else if (chatBroadcast.getSearchLastActionFrom () != null) {

			searchParams.add (
				stringFormat (
					"last action from %s",
					timeFormatter.instantToTimestampString (
						chatMiscLogic.timezone (
							chat),
						dateToInstant (
							chatBroadcast.getSearchLastActionFrom ()))));

		} else if (chatBroadcast.getSearchLastActionTo () != null) {

			searchParams.add (
				stringFormat (
					"last action to %s",
					timeFormatter.instantToTimestampString (
						chatMiscLogic.timezone (
							chat),
						dateToInstant (
							chatBroadcast.getSearchLastActionTo ()))));

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
