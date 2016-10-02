package wbs.apn.chat.user.core.console;

import static wbs.utils.etc.LogicUtils.booleanToYesNo;
import static wbs.utils.etc.LogicUtils.ifNotNullThenElse;
import static wbs.utils.etc.LogicUtils.ifNotNullThenElseEmDash;
import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.lessThan;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.camelToSpaces;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableDetailsRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableDetailsRowWriteHtml;
import static wbs.utils.web.HtmlTableUtils.htmlTableDetailsRowWriteRaw;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenDetails;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowSeparatorWrite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import wbs.apn.chat.bill.logic.ChatCreditCheckResult;
import wbs.apn.chat.bill.logic.ChatCreditLogic;
import wbs.apn.chat.core.logic.ChatLogicHooks;
import wbs.apn.chat.core.logic.ChatLogicHooks.ChatUserCharge;
import wbs.apn.chat.core.logic.ChatMiscLogic;
import wbs.apn.chat.scheme.model.ChatSchemeChargesRec;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.ChatUserType;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.platform.currency.logic.CurrencyLogic;
import wbs.platform.media.console.MediaConsoleLogic;
import wbs.sms.gazetteer.logic.GazetteerLogic;
import wbs.utils.time.TimeFormatter;

@PrototypeComponent ("chatUserSummaryPart")
public
class ChatUserSummaryPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ChatCreditLogic chatCreditLogic;

	@SingletonDependency
	ChatLogicHooks chatHooks;

	@SingletonDependency
	ChatMiscLogic chatLogic;

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@SingletonDependency
	ChatUserLogic chatUserLogic;

	@SingletonDependency
	CurrencyLogic currencyLogic;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	GazetteerLogic gazetteerLogic;

	@SingletonDependency
	MediaConsoleLogic mediaConsoleLogic;

	@SingletonDependency
	TimeFormatter timeFormatter;

	// state

	ChatUserRec chatUser;
	ChatCreditCheckResult creditCheckResult;

	List<ChatLogicHooks.ChatUserCharge>
		internalChatUserCharges,
		externalChatUserCharges;

	// implementation

	@Override
	public
	void prepare () {

		chatUser =
			chatUserHelper.findRequired (
				requestContext.stuffInteger (
					"chatUserId"));

		creditCheckResult =
			chatCreditLogic.userCreditCheck (
				chatUser);

		internalChatUserCharges =
			new ArrayList<ChatLogicHooks.ChatUserCharge> ();

		externalChatUserCharges =
			new ArrayList<ChatLogicHooks.ChatUserCharge> ();

		chatHooks.collectChatUserCharges (
			chatUser,
			internalChatUserCharges,
			externalChatUserCharges);

		Comparator<ChatLogicHooks.ChatUserCharge> comparator =
			new Comparator<ChatLogicHooks.ChatUserCharge> () {

			@Override
			public
			int compare (
					ChatUserCharge left,
					ChatUserCharge right) {

				return left.name.compareTo (
					right.name);

			}

		};

		Collections.sort (
			internalChatUserCharges,
			comparator);

		Collections.sort (
			externalChatUserCharges,
			comparator);
	}

	@Override
	public
	void renderHtmlBodyContent () {

		boolean isUser =
			chatUser.getType () == ChatUserType.user;

		htmlTableOpenDetails ();

		htmlTableDetailsRowWrite (
			"Id",
			integerToDecimalString (
				chatUser.getId ()));

		htmlTableDetailsRowWrite (
			"Code",
			chatUser.getCode ());

		htmlTableDetailsRowWrite (
			"Type",
			chatUser.getType ().name ());

		htmlTableDetailsRowWrite (
			"Gender",
			ifNotNullThenElseEmDash (
				chatUser.getGender (),
				() -> chatUser.getGender ().name ()));

		htmlTableDetailsRowWrite (
			"Orient",
			ifNotNullThenElseEmDash (
				chatUser.getOrient (),
				() -> chatUser.getOrient ().name ()));

		htmlTableDetailsRowWrite (
			"Operator label",
			ifNotNullThenElseEmDash (
				chatUser.getOperatorLabel (),
				() -> chatUser.getOperatorLabel ().name ()));

		htmlTableDetailsRowWrite (
			"Date of birth",
			ifNotNullThenElseEmDash (
				chatUser.getDob (),
				() ->
					timeFormatter.dateString (
						chatUser.getDob ())));

		htmlTableDetailsRowWrite (
			"Name",
			ifNotNullThenElseEmDash (
				chatUser.getName (),
				() -> chatUser.getName ()));

		htmlTableDetailsRowWrite (
			"Info",
			ifNotNullThenElseEmDash (
				chatUser.getInfoText (),
				() -> chatUser.getInfoText ().getText ()));

		if (! chatUser.getChatUserImageList ().isEmpty ()) {

			htmlTableDetailsRowWriteHtml (
				"Pic",
				() -> mediaConsoleLogic.writeMediaThumb100 (
					chatUser.getChatUserImageList ().get (0).getMedia ()));

		}

		htmlTableDetailsRowWriteRaw (
			"Location",
			ifNotNullThenElseEmDash (
				chatUser.getLocationLongLat (),
				() -> gazetteerLogic.findNearestCanonicalEntry (
					chatUser.getChat ().getGazetteer (),
					chatUser.getLocationLongLat ()
				).getName ()));

		htmlTableDetailsRowWrite (
			"Online",
			booleanToYesNo (
				chatUser.getOnline ()));

		if (isUser) {

			htmlTableDetailsRowWrite (
				"First join",
				ifNotNullThenElseEmDash (
					chatUser.getFirstJoin (),
					() -> timeFormatter.timestampTimezoneString (
						chatUserLogic.getTimezone (
							chatUser),
						chatUser.getFirstJoin ())));

			htmlTableDetailsRowWrite (
				"Last join",
				ifNotNullThenElseEmDash (
					chatUser.getLastJoin (),
					() -> timeFormatter.timestampTimezoneString (
						chatUserLogic.getTimezone (
							chatUser),
						chatUser.getLastJoin ())));

		}

		if (isUser) {

			ChatSchemeChargesRec charges =
				chatUser.getChatScheme () != null
					? chatUser.getChatScheme ().getCharges ()
					: null;

			htmlTableRowSeparatorWrite ();

			htmlTableDetailsRowWriteRaw (
				"Number",
				() -> objectManager.writeTdForObjectMiniLink (
					chatUser.getOldNumber ()));

			htmlTableDetailsRowWriteRaw (
				"Scheme",
				() -> objectManager.writeTdForObjectMiniLink (
					chatUser.getChatScheme (),
					chatUser.getChat ()));

			htmlTableDetailsRowWriteRaw (
				"Affiliate",
				() -> ifNotNullThenElse (
					chatUser.getChatAffiliate (),
					() -> objectManager.writeTdForObjectMiniLink (
						chatUser.getChatAffiliate (),
						chatUser.getChatScheme ()),
					() -> htmlTableCellWrite (
						"—")));

			htmlTableDetailsRowWrite (
				"Barred",
				booleanToYesNo (
					chatUser.getBarred ()));

			htmlTableDetailsRowWrite (
				"Adult verified",
				booleanToYesNo (
					chatUser.getAdultVerified ()));

			htmlTableRowSeparatorWrite ();

			htmlTableDetailsRowWriteHtml (
				"User messages",
				stringFormat (
					"%s (%h)",
					currencyLogic.formatHtml (
						chatUser.getChat ().getCurrency (),
						chatUser.getUserMessageCharge ()),
					chatUser.getUserMessageCount ()));

			htmlTableDetailsRowWriteHtml (
				"Monitor messages",
				stringFormat (
					"%s (%h)",
					currencyLogic.formatHtml (
						chatUser.getChat ().getCurrency (),
						chatUser.getMonitorMessageCharge ()),
					chatUser.getMonitorMessageCount ()));

			htmlTableDetailsRowWriteHtml (
				"Text profile",
				stringFormat (
					"%s (%h)",
					currencyLogic.formatHtml (
						chatUser.getChat ().getCurrency (),
						chatUser.getTextProfileCharge ()),
					chatUser.getTextProfileCount ()));

			htmlTableDetailsRowWriteHtml (
				"Image profile",
				stringFormat (
					"%s (%h)",
					currencyLogic.formatHtml (
						chatUser.getChat ().getCurrency (),
						chatUser.getImageProfileCharge ()),
					chatUser.getImageProfileCount ()));

			htmlTableDetailsRowWriteHtml (
				"Video profile",
				stringFormat (
					"%s (%h)",
					currencyLogic.formatHtml (
						chatUser.getChat ().getCurrency (),
						chatUser.getVideoProfileCharge ()),
					chatUser.getVideoProfileCount ()));

			htmlTableDetailsRowWriteHtml (
				"Received message",
				stringFormat (
					"%s (%h)",
					currencyLogic.formatHtml (
						chatUser.getChat ().getCurrency (),
						chatUser.getReceivedMessageCharge ()),
					chatUser.getReceivedMessageCount ()));

			for (
				ChatLogicHooks.ChatUserCharge chatUserCharge
					: internalChatUserCharges
			) {

				htmlTableDetailsRowWriteHtml (
					chatUserCharge.name,
					stringFormat (
						"%s (%h)",
						chatUserCharge.name,
						currencyLogic.formatHtml (
							chatUser.getChat ().getCurrency (),
							chatUserCharge.charge),
						chatUserCharge.count));

			}

			htmlTableDetailsRowWriteHtml (
				"Total spent",
				currencyLogic.formatHtml (
					chatUser.getChat ().getCurrency (),

					chatUser.getValueSinceEver ()));

			htmlTableRowSeparatorWrite ();

			long total =
				chatUser.getValueSinceEver ();

			for (
				ChatLogicHooks.ChatUserCharge chatUserCharge
					: externalChatUserCharges
			) {

				htmlTableDetailsRowWriteHtml (
					chatUserCharge.name,
					stringFormat (
						"%s (%h)",
						chatUserCharge.name,
						currencyLogic.formatHtml (
							chatUser.getChat ().getCurrency (),
							chatUserCharge.charge),
						chatUserCharge.count));

				total += chatUserCharge.charge;

			}

			htmlTableDetailsRowWriteHtml (
				"Grand total",
				currencyLogic.formatHtml (
					chatUser.getChat ().getCurrency (),
					total));

			htmlTableRowSeparatorWrite ();

			htmlTableDetailsRowWriteHtml (
				"Credit",
				currencyLogic.formatHtml (
					chatUser.getChat ().getCurrency (),
					chatUser.getCredit ()));

			htmlTableDetailsRowWrite (
				"Credit mode",
				camelToSpaces (
					chatUser.getCreditMode ().name ()));

			htmlTableDetailsRowWrite (
				"Credit check",
				creditCheckResult.details ());

			htmlTableDetailsRowWriteHtml (
				"Credit pending strict",
				currencyLogic.formatHtml (
					chatUser.getChat ().getCurrency (),
					chatUser.getCreditPendingStrict ()));

			htmlTableDetailsRowWriteHtml (
				"Credit success",
				currencyLogic.formatHtml (
					chatUser.getChat ().getCurrency (),
					chatUser.getCreditSuccess ()));

			htmlTableDetailsRowWriteHtml (
				"Credit awaiting retry",
				currencyLogic.formatHtml (
					chatUser.getChat ().getCurrency (),
					chatUser.getCreditRevoked ()));

			htmlTableDetailsRowWriteHtml (
				"Free usage",
				currencyLogic.formatHtml (
					chatUser.getChat ().getCurrency (),
					chatUser.getCreditAdded ()));

			htmlTableDetailsRowWriteHtml (
				"Credit bought/given",
				currencyLogic.formatHtml (
					chatUser.getChat ().getCurrency (),
					chatUser.getCreditBought ()));

			htmlTableDetailsRowWriteHtml (
				"Credit limit",
				ifThenElse (
					isNotNull (
						charges)
					&& lessThan (
						charges.getCreditLimit (),
						chatUser.getCreditLimit ()),

				() -> stringFormat (
					"%s (%s)",
					currencyLogic.formatHtml (
						chatUser.getChat ().getCurrency (),
						charges.getCreditLimit ()),
					currencyLogic.formatHtml (
						chatUser.getChat ().getCurrency (),
						chatUser.getCreditLimit ())),

				() -> currencyLogic.formatHtml (
					chatUser.getChat ().getCurrency (),
					chatUser.getCreditLimit ()))

			);

			htmlTableRowSeparatorWrite ();

			htmlTableDetailsRowWriteHtml (
				"Daily Billed",
				currencyLogic.formatHtml (
					chatUser.getChat ().getCurrency (),
					chatUser.getCreditDailyAmount ()));

			htmlTableDetailsRowWriteHtml (
				"Last bill date",
				ifNotNullThenElseEmDash (
					chatUser.getCreditDailyDate (),
					() -> timeFormatter.dateString (
						chatUser.getCreditDailyDate ())));

		}

		htmlTableClose ();

	}

}
