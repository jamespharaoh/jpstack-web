package wbs.apn.chat.user.core.console;

import static wbs.utils.etc.EnumUtils.enumEqualSafe;
import static wbs.utils.etc.LogicUtils.booleanToYesNo;
import static wbs.utils.etc.LogicUtils.ifNotNullThenElse;
import static wbs.utils.etc.LogicUtils.ifNotNullThenElseEmDash;
import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.Misc.lessThan;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.camelToSpaces;
import static wbs.utils.string.StringUtils.stringFormatLazy;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWriteHtml;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWriteRaw;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowSeparatorWrite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import lombok.NonNull;

import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.platform.currency.logic.CurrencyLogic;
import wbs.platform.media.console.MediaConsoleLogic;

import wbs.sms.gazetteer.logic.GazetteerLogic;

import wbs.utils.string.FormatWriter;
import wbs.utils.time.TimeFormatter;

import wbs.apn.chat.bill.logic.ChatCreditCheckResult;
import wbs.apn.chat.bill.logic.ChatCreditLogic;
import wbs.apn.chat.core.logic.ChatLogicHooks;
import wbs.apn.chat.core.logic.ChatLogicHooks.ChatUserCharge;
import wbs.apn.chat.core.logic.ChatMiscLogic;
import wbs.apn.chat.scheme.model.ChatSchemeChargesRec;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.ChatUserType;

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
	GazetteerLogic gazetteerLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MediaConsoleLogic mediaConsoleLogic;

	@SingletonDependency
	ConsoleObjectManager objectManager;

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
	void prepare (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepare");

		) {

			chatUser =
				chatUserHelper.findFromContextRequired (
					transaction);

			creditCheckResult =
				chatCreditLogic.userCreditCheck (
					transaction,
					chatUser);

			internalChatUserCharges =
				new ArrayList<> ();

			externalChatUserCharges =
				new ArrayList<> ();

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

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContent");

		) {

			boolean isUser =
				enumEqualSafe (
					chatUser.getType (),
					ChatUserType.user);

			htmlTableOpenDetails (
				formatWriter);

			htmlTableDetailsRowWrite (
				formatWriter,
				"Id",
				integerToDecimalString (
					chatUser.getId ()));

			htmlTableDetailsRowWrite (
				formatWriter,
				"Code",
				chatUser.getCode ());

			htmlTableDetailsRowWrite (
				formatWriter,
				"Type",
				chatUser.getType ().name ());

			htmlTableDetailsRowWrite (
				formatWriter,
				"Gender",
				ifNotNullThenElseEmDash (
					chatUser.getGender (),
					() -> chatUser.getGender ().name ()));

			htmlTableDetailsRowWrite (
				formatWriter,
				"Orient",
				ifNotNullThenElseEmDash (
					chatUser.getOrient (),
					() -> chatUser.getOrient ().name ()));

			htmlTableDetailsRowWrite (
				formatWriter,
				"Operator label",
				ifNotNullThenElseEmDash (
					chatUser.getOperatorLabel (),
					() -> chatUser.getOperatorLabel ().name ()));

			htmlTableDetailsRowWrite (
				formatWriter,
				"Date of birth",
				ifNotNullThenElseEmDash (
					chatUser.getDob (),
					() ->
						timeFormatter.dateString (
							chatUser.getDob ())));

			htmlTableDetailsRowWrite (
				formatWriter,
				"Name",
				ifNotNullThenElseEmDash (
					chatUser.getName (),
					() -> chatUser.getName ()));

			htmlTableDetailsRowWrite (
				formatWriter,
				"Info",
				ifNotNullThenElseEmDash (
					chatUser.getInfoText (),
					() -> chatUser.getInfoText ().getText ()));

			if (! chatUser.getChatUserImageList ().isEmpty ()) {

				htmlTableDetailsRowWriteHtml (
					formatWriter,
					"Pic",
					() -> mediaConsoleLogic.writeMediaThumb100 (
						transaction,
						formatWriter,
						chatUser.getChatUserImageList ().get (0).getMedia ()));

			}

			htmlTableDetailsRowWriteRaw (
				formatWriter,
				"Location",
				ifNotNullThenElseEmDash (
					chatUser.getLocationLongLat (),
					() -> gazetteerLogic.findNearestCanonicalEntry (
						chatUser.getChat ().getGazetteer (),
						chatUser.getLocationLongLat ()
					).getName ()));

			htmlTableDetailsRowWrite (
				formatWriter,
				"Online",
				booleanToYesNo (
					chatUser.getOnline ()));

			if (isUser) {

				htmlTableDetailsRowWrite (
					formatWriter,
					"First join",
					ifNotNullThenElseEmDash (
						chatUser.getFirstJoin (),
						() -> timeFormatter.timestampTimezoneString (
							chatUserLogic.getTimezone (
								chatUser),
							chatUser.getFirstJoin ())));

				htmlTableDetailsRowWrite (
					formatWriter,
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

				htmlTableRowSeparatorWrite (
					formatWriter);

				htmlTableDetailsRowWriteRaw (
					formatWriter,
					"Number",
					() -> objectManager.writeTdForObjectMiniLink (
						transaction,
						formatWriter,
						chatUser.getOldNumber ()));

				htmlTableDetailsRowWriteRaw (
					formatWriter,
					"Scheme",
					() -> objectManager.writeTdForObjectMiniLink (
						transaction,
						formatWriter,
						chatUser.getChatScheme (),
						chatUser.getChat ()));

				htmlTableDetailsRowWriteRaw (
					formatWriter,
					"Affiliate",
					() -> ifNotNullThenElse (
						chatUser.getChatAffiliate (),
						() -> objectManager.writeTdForObjectMiniLink (
							transaction,
							formatWriter,
							chatUser.getChatAffiliate (),
							chatUser.getChatScheme ()),
						() -> htmlTableCellWrite (
							formatWriter,
							"—")));

				htmlTableDetailsRowWrite (
					formatWriter,
					"Barred",
					booleanToYesNo (
						chatUser.getBarred ()));

				htmlTableDetailsRowWrite (
					formatWriter,
					"Adult verified",
					booleanToYesNo (
						chatUser.getAdultVerified ()));

				htmlTableRowSeparatorWrite (
					formatWriter);

				htmlTableDetailsRowWriteHtml (
					formatWriter,
					"User messages",
					stringFormatLazy (
						"%s (%h)",
						currencyLogic.formatHtml (
							chatUser.getChat ().getCurrency (),
							chatUser.getUserMessageCharge ()),
						integerToDecimalString (
							chatUser.getUserMessageCount ())));

				htmlTableDetailsRowWriteHtml (
					formatWriter,
					"Monitor messages",
					stringFormatLazy (
						"%s (%h)",
						currencyLogic.formatHtml (
							chatUser.getChat ().getCurrency (),
							chatUser.getMonitorMessageCharge ()),
						integerToDecimalString (
							chatUser.getMonitorMessageCount ())));

				htmlTableDetailsRowWriteHtml (
					formatWriter,
					"Text profile",
					stringFormatLazy (
						"%s (%h)",
						currencyLogic.formatHtml (
							chatUser.getChat ().getCurrency (),
							chatUser.getTextProfileCharge ()),
						integerToDecimalString (
							chatUser.getTextProfileCount ())));

				htmlTableDetailsRowWriteHtml (
					formatWriter,
					"Image profile",
					stringFormatLazy (
						"%s (%h)",
						currencyLogic.formatHtml (
							chatUser.getChat ().getCurrency (),
							chatUser.getImageProfileCharge ()),
						integerToDecimalString (
							chatUser.getImageProfileCount ())));

				htmlTableDetailsRowWriteHtml (
					formatWriter,
					"Video profile",
					stringFormatLazy (
						"%s (%h)",
						currencyLogic.formatHtml (
							chatUser.getChat ().getCurrency (),
							chatUser.getVideoProfileCharge ()),
						integerToDecimalString (
							chatUser.getVideoProfileCount ())));

				htmlTableDetailsRowWriteHtml (
					formatWriter,
					"Received message",
					stringFormatLazy (
						"%s (%h)",
						currencyLogic.formatHtml (
							chatUser.getChat ().getCurrency (),
							chatUser.getReceivedMessageCharge ()),
						integerToDecimalString (
							chatUser.getReceivedMessageCount ())));

				for (
					ChatLogicHooks.ChatUserCharge chatUserCharge
						: internalChatUserCharges
				) {

					htmlTableDetailsRowWriteHtml (
						formatWriter,
						chatUserCharge.name,
						stringFormatLazy (
							"%s (%h)",
							chatUserCharge.name,
							currencyLogic.formatHtml (
								chatUser.getChat ().getCurrency (),
								chatUserCharge.charge),
							integerToDecimalString (
								chatUserCharge.count)));

				}

				htmlTableDetailsRowWriteHtml (
					formatWriter,
					"Total spent",
					currencyLogic.formatHtml (
						chatUser.getChat ().getCurrency (),

						chatUser.getValueSinceEver ()));

				htmlTableRowSeparatorWrite (
					formatWriter);

				long total =
					chatUser.getValueSinceEver ();

				for (
					ChatLogicHooks.ChatUserCharge chatUserCharge
						: externalChatUserCharges
				) {

					htmlTableDetailsRowWriteHtml (
						formatWriter,
						chatUserCharge.name,
						stringFormatLazy (
							"%s (%h)",
							chatUserCharge.name,
							currencyLogic.formatHtml (
								chatUser.getChat ().getCurrency (),
								chatUserCharge.charge),
							integerToDecimalString (
								chatUserCharge.count)));

					total += chatUserCharge.charge;

				}

				htmlTableDetailsRowWriteHtml (
					formatWriter,
					"Grand total",
					currencyLogic.formatHtml (
						chatUser.getChat ().getCurrency (),
						total));

				htmlTableRowSeparatorWrite (
					formatWriter);

				htmlTableDetailsRowWriteHtml (
					formatWriter,
					"Credit",
					currencyLogic.formatHtml (
						chatUser.getChat ().getCurrency (),
						chatUser.getCredit ()));

				htmlTableDetailsRowWrite (
					formatWriter,
					"Credit mode",
					camelToSpaces (
						chatUser.getCreditMode ().name ()));

				htmlTableDetailsRowWrite (
					formatWriter,
					"Credit check",
					creditCheckResult.details ());

				htmlTableDetailsRowWriteHtml (
					formatWriter,
					"Credit pending strict",
					currencyLogic.formatHtml (
						chatUser.getChat ().getCurrency (),
						chatUser.getCreditPendingStrict ()));

				htmlTableDetailsRowWriteHtml (
					formatWriter,
					"Credit success",
					currencyLogic.formatHtml (
						chatUser.getChat ().getCurrency (),
						chatUser.getCreditSuccess ()));

				htmlTableDetailsRowWriteHtml (
					formatWriter,
					"Credit awaiting retry",
					currencyLogic.formatHtml (
						chatUser.getChat ().getCurrency (),
						chatUser.getCreditRevoked ()));

				htmlTableDetailsRowWriteHtml (
					formatWriter,
					"Free usage",
					currencyLogic.formatHtml (
						chatUser.getChat ().getCurrency (),
						chatUser.getCreditAdded ()));

				htmlTableDetailsRowWriteHtml (
					formatWriter,
					"Credit bought/given",
					currencyLogic.formatHtml (
						chatUser.getChat ().getCurrency (),
						chatUser.getCreditBought ()));

				htmlTableDetailsRowWriteHtml (
					formatWriter,
					"Credit limit",
					ifThenElse (
						isNotNull (
							charges)
						&& lessThan (
							charges.getCreditLimit (),
							chatUser.getCreditLimit ()),

					() -> stringFormatLazy (
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

				htmlTableRowSeparatorWrite (
					formatWriter);

				htmlTableDetailsRowWriteHtml (
					formatWriter,
					"Daily Billed",
					currencyLogic.formatHtml (
						chatUser.getChat ().getCurrency (),
						chatUser.getCreditDailyAmount ()));

				htmlTableDetailsRowWriteHtml (
					formatWriter,
					"Last bill date",
					ifNotNullThenElseEmDash (
						chatUser.getCreditDailyDate (),
						() -> timeFormatter.dateString (
							chatUser.getCreditDailyDate ())));

			}

			htmlTableClose (
				formatWriter);

		}

	}

}
