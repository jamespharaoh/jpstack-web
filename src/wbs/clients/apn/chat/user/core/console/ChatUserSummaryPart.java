package wbs.clients.apn.chat.user.core.console;

import static wbs.framework.utils.etc.Misc.dateToInstant;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import wbs.clients.apn.chat.bill.logic.ChatCreditCheckResult;
import wbs.clients.apn.chat.bill.logic.ChatCreditLogic;
import wbs.clients.apn.chat.core.logic.ChatLogicHooks;
import wbs.clients.apn.chat.core.logic.ChatLogicHooks.ChatUserCharge;
import wbs.clients.apn.chat.core.logic.ChatMiscLogic;
import wbs.clients.apn.chat.scheme.model.ChatSchemeChargesRec;
import wbs.clients.apn.chat.user.core.logic.ChatUserLogic;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.core.model.ChatUserType;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.misc.TimeFormatter;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.cal.CalDate;
import wbs.platform.currency.logic.CurrencyLogic;
import wbs.platform.media.console.MediaConsoleLogic;
import wbs.sms.gazetteer.logic.GazetteerLogic;

@PrototypeComponent ("chatUserSummaryPart")
public
class ChatUserSummaryPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	ChatCreditLogic chatCreditLogic;

	@Inject
	ChatLogicHooks chatHooks;

	@Inject
	ChatMiscLogic chatLogic;

	@Inject
	ChatUserConsoleHelper chatUserHelper;

	@Inject
	ChatUserLogic chatUserLogic;

	@Inject
	CurrencyLogic currencyLogic;

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	GazetteerLogic gazetteerLogic;

	@Inject
	MediaConsoleLogic mediaConsoleLogic;

	@Inject
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
			chatUserHelper.find (
				requestContext.stuffInt ("chatUserId"));

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
			public int compare (
					ChatUserCharge left,
					ChatUserCharge right) {

				return left.name.compareTo (right.name);

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

		printFormat (
			"<table class=\"details\">\n");

		printFormat (
			"<tr> <th>Id</th> <td>%h</td> </tr>\n",
			chatUser.getId ());

		printFormat (
			"<tr> <th>Code</th> <td>%h</td> </tr>\n",
			chatUser.getCode ());

		printFormat (
			"<tr> <th>Type</th> <td>%h</td> </tr>\n",
			chatUser.getType ());

		printFormat (
			"<tr> <th>Gender</th> <td>%h</td> </tr>\n",
			ifNull (
				chatUser.getGender (),
				"-"));

		printFormat (
			"<tr>\n",
			"<th>Orient</th>\n",

			"<td>%h</td>\n",
			ifNull (
				chatUser.getOrient (),
				"-"),

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Operator label</th>\n",

			"<td>%h</td>\n",
			chatUser.getOperatorLabel (),

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Date of birth</th>\n",

			"<td>%h</td>\n",
			chatUser.getDob () != null
				? CalDate.forLocalDate (
					chatUser.getDob ())
				: "-",

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Name</th>\n",

			"<td>%h</td>\n",
			ifNull (
				chatUser.getName (),
				"-"),

			"</tr>\n");

		printFormat (
			"<tr> <th>Info</th> <td>%h</td> </tr>\n",
			chatUser.getInfoText () != null
				? chatUser.getInfoText ().getText ()
				: "-");

		if (! chatUser.getChatUserImageList ().isEmpty ()) {

			printFormat (
				"<tr>\n",
				"<th>Pic</th>\n",
				"<td>%s</td>\n",
				mediaConsoleLogic.mediaThumb100 (
					chatUser.getChatUserImageList ().get (0).getMedia ()),
				"</tr>\n");

		} else {

			printFormat (
				"<tr>\n",
				"<th>Pic</th>\n",
				"<td>-</td>\n",
				"</tr>\n");

		}

		printFormat (
			"<tr>\n",
			"<th>Location</th>\n",
			"<td>%s</td>\n",
			chatUser.getLocationLongLat () != null
				? gazetteerLogic.findNearestCanonicalEntry (
						chatUser.getChat ().getGazetteer (),
						chatUser.getLocationLongLat ()
					).getName ()
				: "-",
			"</tr>\n");

		printFormat (
			"<tr> <th>Online</th> <td>%h</td> </tr>\n",
				chatUser.getOnline() ? "yes" : "no");

		if (isUser) {

			printFormat (
				"<tr>\n",

				"<th>First join</th>\n",

				"<td>%h</td>\n",
				chatUser.getFirstJoin () != null
					? timeFormatter.instantToTimestampString (
						chatUserLogic.timezone (
							chatUser),
						dateToInstant (
							chatUser.getFirstJoin ()))
					: "-",

				"</tr>\n");

			printFormat (
				"<tr>\n",

				"<th>Last join</th>\n",

				"<td>%h</td>\n",
				chatUser.getLastJoin () != null
					? timeFormatter.instantToTimestampString (
						chatUserLogic.timezone (
							chatUser),
						dateToInstant (
							chatUser.getLastJoin ()))
					: "-",

				"</tr>\n");

		}

		if (isUser) {

			ChatSchemeChargesRec charges =
				chatUser.getChatScheme () != null ?
					chatUser.getChatScheme ().getCharges ()
					: null;

			printFormat (
				"<tr class=\"sep\">\n");

			printFormat (
				"<tr>\n",
				"<th>Number</th>\n",
				"%s\n",
				objectManager.tdForObjectMiniLink (
					chatUser.getOldNumber ()),
				"</tr>\n");

			printFormat (
				"<tr>\n",
				"<th>Scheme</th>\n",
				"%s\n",
				objectManager.tdForObjectMiniLink (
					chatUser.getChatScheme (),
					chatUser.getChat ()),
				"</tr>\n");

			printFormat (
				"<tr> <th>Affiliate</th> %s </tr>\n",
				objectManager.tdForObjectMiniLink (
					chatUser.getChatAffiliate (),
					chatUser.getChatScheme ()));

			// "<tr> <th>Block all</th> <td>%h</td> </tr>\n",
			// chatUser.getBlockAll ()? "yes" : "no",
			// "<tr> <th>Age confirmed</th> <td>%h</td> </tr>\n",
			// chatUser.getAgeChecked ()? "yes" : "no",

			printFormat (
				"<tr> <th>Barred</th> <td>%h</td> </tr>\n",
				chatUser.getBarred () ? "yes" : "no");

			printFormat (
				"<tr> <th>Adult verified</th> <td>%h</td> </tr>\n",
				chatUser.getAdultVerified () ? "yes" : "no");

			// "<tr> <th>Rejection count</th> <td>%h</td> </tr>\n",
			// chatUser.getRejectionCount (),

			printFormat (
				"<tr class=\"sep\">\n");

			printFormat (
				"<tr> <th>User messages</th> <td>%s (%h)</td> </tr>\n",
				currencyLogic.formatHtml (
					chatUser.getChat ().getCurrency (),
					Long.valueOf(chatUser.getUserMessageCharge ())),
				chatUser.getUserMessageCount ());

			printFormat (
				"<tr> <th>Monitor messages</th> <td>%s (%h)</td> </tr>\n",
				currencyLogic.formatHtml (
					chatUser.getChat ().getCurrency (),
					Long.valueOf(chatUser.getMonitorMessageCharge ())),
				chatUser.getMonitorMessageCount ());

			printFormat (
				"<tr> <th>Text profile</th> <td>%s (%h)</td> </tr>\n",
				currencyLogic.formatHtml (
					chatUser.getChat ().getCurrency (),
					Long.valueOf(chatUser.getTextProfileCharge ())),
				chatUser.getTextProfileCount ());

			printFormat (
				"<tr>\n",
				"<th>Image profile</th>\n",
				"<td>%s (%h)</td>\n",
				currencyLogic.formatHtml (
					chatUser.getChat ().getCurrency (),
					Long.valueOf(chatUser.getImageProfileCharge ())),
				chatUser.getImageProfileCount (),
				"</tr>\n");

			printFormat (
				"<tr> <th>Video profile</th> <td>%s (%h)</td> </tr>\n",
				currencyLogic.formatHtml (
					chatUser.getChat ().getCurrency (),
					Long.valueOf(chatUser.getVideoProfileCharge ())),
				chatUser.getVideoProfileCount ());

			printFormat (
				"<tr>\n",
				"<th>Received message</th>\n",

				"<td>%s (%h)</td>\n",
				currencyLogic.formatHtml (
					chatUser.getChat ().getCurrency (),
					Long.valueOf(chatUser.getReceivedMessageCharge ())),
				chatUser.getReceivedMessageCount (),

				"</tr>\n");

			for (ChatLogicHooks.ChatUserCharge chatUserCharge
					: internalChatUserCharges) {

				printFormat (
					"<tr>\n",
					"<th>%h</th>\n",

					"<td>%s (%h)</td>\n",
					chatUserCharge.name,
					currencyLogic.formatHtml (
						chatUser.getChat ().getCurrency (),
						Long.valueOf(chatUserCharge.charge)),
					chatUserCharge.count,

					"</tr>\n");

			}

			printFormat (
				"<tr>\n",
				"<th>Total spent</th>\n",

				"<td>%s</td>\n",
				currencyLogic.formatHtml (
					chatUser.getChat ().getCurrency (),
					Long.valueOf(chatUser.getValueSinceEver ())),

				"</tr>\n");

			printFormat (
				"<tr class=\"sep\">\n");

			int total =
				chatUser.getValueSinceEver ();

			for (ChatLogicHooks.ChatUserCharge chatUserCharge
					: externalChatUserCharges) {

				printFormat (
					"<tr> <th>%h</th> <td>%s (%h)</td> </tr>\n",
					chatUserCharge.name,
					currencyLogic.formatHtml (
						chatUser.getChat ().getCurrency (),
						Long.valueOf(chatUserCharge.charge)),
					chatUserCharge.count);

				total += chatUserCharge.charge;
			}

			printFormat (
				"<tr> <th>Grand total</th> <td>%s</td> </tr>\n",
				currencyLogic.formatHtml (
					chatUser.getChat ().getCurrency (),
					Long.valueOf(total)));

			printFormat (
				"<tr class=\"sep\">\n");

			printFormat (
				"<tr> <th>Credit</th> <td>%s</td> </tr>\n",
				currencyLogic.formatHtml (
					chatUser.getChat ().getCurrency (),
					Long.valueOf(chatUser.getCredit ())));

			printFormat (
				"<tr> <th>Credit mode</th> <td>%h</td> </tr>\n",
				chatUser.getCreditMode ());

			printFormat (
				"<tr>\n",
				"<th>Credit check</th>\n",
				"<td>%h</td>\n",
				creditCheckResult.details (),
				"</tr>\n");

			printFormat (
				"<tr>\n",
				"<th>Credit pending strict</th>\n",
				"<td>%s</td>\n",
				currencyLogic.formatHtml (
					chatUser.getChat ().getCurrency (),
					Long.valueOf(chatUser.getCreditPendingStrict ())),
				"</tr>\n");

			printFormat (
				"<tr> <th>Credit success</th> <td>%s</td> </tr>\n",
				currencyLogic.formatHtml (
					chatUser.getChat ().getCurrency (),
					Long.valueOf(chatUser.getCreditSuccess ())));

			printFormat (
				"<tr> <th>Credit awaiting retry</th> <td>%s</td> </tr>\n",
				currencyLogic.formatHtml (
					chatUser.getChat ().getCurrency (),
					Long.valueOf(chatUser.getCreditRevoked ())));

			printFormat (
				"<tr> <th>Free usage</th> <td>%s</td> </tr>\n",
				currencyLogic.formatHtml (
					chatUser.getChat ().getCurrency (),
					Long.valueOf(chatUser.getCreditAdded ())));

			printFormat (
				"<tr> <th>Credit bought/given</th> <td>%s</td> </tr>\n",
				currencyLogic.formatHtml (
					chatUser.getChat ().getCurrency (),
					Long.valueOf(chatUser.getCreditBought ())));

			printFormat (
				"<tr>\n",
				"<th>Credit limit</th>\n",

				"<td>%s</td>\n",
				charges != null
					&& charges.getCreditLimit ()
						< chatUser.getCreditLimit ()

					? stringFormat (
						"%s (%s)",
						currencyLogic.formatHtml (
							chatUser.getChat ().getCurrency (),
							Long.valueOf(charges.getCreditLimit ())),
						currencyLogic.formatHtml (
							chatUser.getChat ().getCurrency (),
							Long.valueOf(chatUser.getCreditLimit ())))

					: currencyLogic.formatHtml (
						chatUser.getChat ().getCurrency (),
						Long.valueOf(chatUser.getCreditLimit ())),

				"</tr>\n");

			printFormat (
				"<tr class=\"sep\">\n");

			printFormat (
				"<tr>\n",
				"<th>Daily Billed</th>\n",
				"<td>%s</td>\n",
				currencyLogic.formatHtml (
					chatUser.getChat ().getCurrency (),
					Long.valueOf(chatUser.getCreditDailyAmount ())),
				"<tr>\n");

			printFormat (
				"<tr>\n",
				"<th>Last bill date</th>\n",
				"<td>%s</td>\n",
				chatUser.getCreditDailyDate () != null
					? chatUser.getCreditDailyDate ().toString ()
					: "-",
				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

}
