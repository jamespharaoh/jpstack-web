package wbs.apn.chat.broadcast.console;

import static wbs.utils.etc.Misc.booleanToYesNo;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.joinWithCommaAndSpace;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenList;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowOpen;

import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;

import wbs.apn.chat.broadcast.model.ChatBroadcastObjectHelper;
import wbs.apn.chat.broadcast.model.ChatBroadcastRec;
import wbs.apn.chat.core.logic.ChatMiscLogic;
import wbs.apn.chat.core.model.ChatObjectHelper;
import wbs.apn.chat.core.model.ChatRec;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.object.ObjectManager;
import wbs.platform.currency.logic.CurrencyLogic;
import wbs.utils.time.TimeFormatter;

@PrototypeComponent ("chatBroadcastListPart")
public
class ChatBroadcastListPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ChatBroadcastObjectHelper chatBroadcastHelper;

	@SingletonDependency
	ChatObjectHelper chatHelper;

	@SingletonDependency
	ChatMiscLogic chatMiscLogic;

	@SingletonDependency
	CurrencyLogic currencyLogic;

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	TimeFormatter timeFormatter;

	// state

	ChatRec chat;
	List <ChatBroadcastRec> broadcasts;

	// implementation

	@Override
	public
	void prepare () {

		chat =
			chatHelper.findRequired (
				requestContext.stuffInteger (
					"chatId"));

		broadcasts =
			chatBroadcastHelper.findRecentWindow (
				chat,
				0,
				100);

	}

	@Override
	public
	void renderHtmlBodyContent () {

		htmlTableOpenList ();

		htmlTableHeaderRowWrite (
			"User",
			"Timestamp",
			"Chat user",
			"Message",
			"Count",
			"Search",
			"Blocked",
			"Opt out");

		for (
			ChatBroadcastRec chatBroadcast
				: broadcasts
		) {

			htmlTableRowOpen ();

			htmlTableCellWrite (
				objectManager.objectPathMini (
					chatBroadcast.getCreatedUser ()));

			htmlTableCellWrite (
				timeFormatter.timestampTimezoneString (
					chatMiscLogic.timezone (
						chat),
					chatBroadcast.getCreatedTime ()));

			htmlTableCellWrite (
				objectManager.objectPathMini (
					chatBroadcast.getChatUser (),
					chat));

			htmlTableCellWrite (
				chatBroadcast.getText ().getText ());

			htmlTableCellWrite (
				integerToDecimalString (
					+ chatBroadcast.getNumAccepted ()
					+ chatBroadcast.getNumSent ()));

			htmlTableCellWrite (
				chatBroadcast.getSearch ()
					? joinWithCommaAndSpace (
						getSearchParams (
							chatBroadcast))
					: "manual number selection");

			htmlTableCellWrite (
				booleanToYesNo (
					chatBroadcast.getIncludeBlocked ()));

			htmlTableCellWrite (
				booleanToYesNo (
					chatBroadcast.getIncludeOptedOut ()));

			htmlTableRowClose ();

		}

		htmlTableClose ();

	}

	List <String> getSearchParams (
			@NonNull ChatBroadcastRec chatBroadcast) {

		List <String> searchParams =
			new ArrayList<> ();

		if (
			chatBroadcast.getSearchLastActionFrom () != null
			&& chatBroadcast.getSearchLastActionTo () != null
		) {

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
