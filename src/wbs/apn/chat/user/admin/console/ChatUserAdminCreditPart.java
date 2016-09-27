package wbs.apn.chat.user.admin.console;

import static wbs.utils.collection.CollectionUtils.collectionIsEmpty;
import static wbs.utils.etc.EnumUtils.enumEqualSafe;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.web.HtmlAttributeUtils.htmlColumnSpanAttribute;
import static wbs.utils.web.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.utils.web.HtmlFormUtils.htmlFormClose;
import static wbs.utils.web.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellWriteHtml;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableDetailsRowWriteHtml;
import static wbs.utils.web.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenDetails;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenList;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowOpen;

import wbs.apn.chat.bill.model.ChatUserCreditRec;
import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.ChatUserType;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.platform.currency.logic.CurrencyLogic;
import wbs.platform.user.console.UserConsoleLogic;

@PrototypeComponent ("chatUserAdminCreditPart")
public
class ChatUserAdminCreditPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	CurrencyLogic currencyLogic;

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@SingletonDependency
	ConsoleObjectManager consoleObjectManager;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// state

	ChatUserRec chatUser;

	// implementation

	@Override
	public
	void prepare () {

		chatUser =
			chatUserHelper.findRequired (
				requestContext.stuffInteger (
					"chatUserId"));

	}

	@Override
	public
	void renderHtmlBodyContent () {

		if (
			enumEqualSafe (
				chatUser.getType (),
				ChatUserType.monitor)
		) {

			formatWriter.writeLineFormat (
				"<p>This is a monitor and cannot be credited.</p>");

			return;

		}

		htmlFormOpenPostAction (
			requestContext.resolveLocalUrl (
				"/chatUser.admin.credit"));

		formatWriter.writeLineFormat (
			"<p>Please note: The credit amount is the actual credit to give ",
			"the user. The bill amount is the amount they have paid. To give ",
			"someone some free credit enter it in credit amount and enter ",
			"0.00 in bill amount. To process a credit card payment enter the ",
			"amount of credit in credit amount and the amount they have paid ",
			"in bill amount.</p>");

		htmlTableOpenDetails ();

		htmlTableDetailsRowWriteHtml (
			"Credit",
			currencyLogic.formatHtml (
				chatUser.getChat ().getCurrency (),
				chatUser.getCredit ()));

		htmlTableDetailsRowWriteHtml (
			"Credit amount",
			stringFormat (
				"%h <input",
				chatUser.getChat ().getCurrency ().getPrefix (),
				" type=\"text\"",
				" name=\"creditAmount\"",
				" value=\"%h\"",
				requestContext.getForm (
					"creditAmount",
					""),
				">"));

		htmlTableDetailsRowWriteHtml (
			"Bill amount",
			stringFormat (
				"%h <input",
				chatUser.getChat ().getCurrency ().getPrefix (),
				" type=\"text\"",
				" name=\"billAmount\"",
				" value=\"%h\"",
				requestContext.getForm (
					"billAmount",
					""),
				">"));

		htmlTableDetailsRowWriteHtml (
			"Details",
			stringFormat (
				"<input",
				" type=\"text\"",
				" name=\"details\"",
				" value=\"%h\"",
				requestContext.getForm (
					"details",
					""),
				">"));

		htmlTableClose ();

		formatWriter.writeLineFormat (
			"<p><input",
			" type=\"submit\"",
			" value=\"credit user\"",
			"></p>");

		htmlFormClose ();

		htmlHeadingTwoWrite (
			"History");

		htmlTableOpenList ();

		htmlTableHeaderRowWrite (
			"Ref",
			"Timestamp",
			"Credit",
			"Bill",
			"Details",
			"User");

		if (
			collectionIsEmpty (
				chatUser.getChatUserCredits ())
		) {

			htmlTableRowOpen ();

			htmlTableCellWrite (
				"Nothing to show",
				htmlColumnSpanAttribute (6l));

			htmlTableRowClose ();

		} else {

			for (
				ChatUserCreditRec chatUserCredit
					: chatUser.getChatUserCredits ()
			) {

				htmlTableRowOpen ();

				htmlTableCellWrite (
					integerToDecimalString (
					chatUserCredit.getId ()));

				htmlTableCellWrite (
					userConsoleLogic.timestampWithoutTimezoneString (
						chatUserCredit.getTimestamp ()));

				htmlTableCellWriteHtml (
					currencyLogic.formatHtml (
						chatUser.getChat ().getCurrency (),
						chatUserCredit.getCreditAmount ()));

				htmlTableCellWriteHtml (
					currencyLogic.formatHtml (
						chatUser.getChat ().getCurrency (),
						chatUserCredit.getBillAmount ()));

				htmlTableCellWrite (
					 chatUserCredit.getDetails ());

				if (
					isNotNull (
						chatUserCredit.getUser ())
				) {

					consoleObjectManager.writeTdForObjectMiniLink (
						chatUserCredit.getUser ());

				} else {

					htmlTableCellWrite (
						"—");

				}

				htmlTableRowClose ();

			}

		}

		htmlTableClose ();

	}

}
