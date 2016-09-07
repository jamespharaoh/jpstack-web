package wbs.clients.apn.chat.user.admin.console;

import static wbs.framework.utils.etc.CollectionUtils.collectionIsEmpty;
import static wbs.framework.utils.etc.EnumUtils.enumEqualSafe;

import wbs.clients.apn.chat.bill.model.ChatUserCreditRec;
import wbs.clients.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.core.model.ChatUserType;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.platform.currency.logic.CurrencyLogic;

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

			printFormat (
				"<p>This is a monitor and cannot be credited.</p>\n");

			return;

		}

		printFormat (
			"<form",
			" method=\"post\"",
			" action=\"%h\"",
			requestContext.resolveLocalUrl (
				"/chatUser.admin.credit"),
			">\n");

		printFormat (
			"<p>Please note: The credit amount is the actual credit to give ",
			"the user. The bill amount is the amount they have paid. To give ",
			"someone some free credit enter it in credit amount and enter ",
			"0.00 in bill amount. To process a credit card payment enter the ",
			"amount of credit in credit amount and the amount they have paid ",
			"in bill amount.</p>");

		printFormat (
			"<table class=\"details\">\n");

		printFormat (
			"<tr>\n",
			"<th>Credit</th>\n",
			"<td>%s</td>\n",
			currencyLogic.formatHtml (
				chatUser.getChat ().getCurrency (),
				Long.valueOf(chatUser.getCredit ())),
			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Credit amount</th>\n");

		printFormat (
			"<td>&#163; ",
			"<input",
			" type=\"text\"",
			" name=\"creditAmount\"",
			" value=\"%h\"",
			requestContext.getForm (
				"amount",
				""),
			"\"></td>\n");

		printFormat (
			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Bill amount</th>\n");

		printFormat (
			"<td>&#163; ",
			"<input",
			" type=\"text\"",
			" name=\"billAmount\"",
			" value=\"%h\"",
			requestContext.getForm (
				"amount",
				""),
			"\"></td>\n");

		printFormat (
			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Details</th>\n");

		printFormat (
			"<td><input",
			" type=\"text\"",
			" name=\"details\"",
			" value=\"%h\"",
			requestContext.getForm (
				"details",
				""),
			"></td>\n");

		printFormat (
			"</tr>\n");

		printFormat (
			"</table>\n");

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" value=\"credit user\"",
			"></p>\n");

		printFormat (
			"</form>\n");

		printFormat (
			"<h2>History</h2>\n");

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th>Ref</th>\n",
			"<th>Timestamp</th>\n",
			"<th>Credit</th>\n",
			"<th>Bill</th>\n",
			"<th>Details</th>\n",
			"<th>User</th>\n",
			"</tr>\n");

		if (
			collectionIsEmpty (
				chatUser.getChatUserCredits ())
		) {

			printFormat (
				"<tr>\n",
				"<td colspan=\"6\">Nothing to show</td>\n",
				"</tr>");

		} else {

			for (
				ChatUserCreditRec chatUserCredit
					: chatUser.getChatUserCredits ()
			) {

				printFormat (
					"<tr>\n");

				printFormat (
					"<td>%h</td>\n",
					chatUserCredit.getId ());

				printFormat (
					"<td>%h</td>\n",
					chatUserCredit.getTimestamp ());

				printFormat (
					"<td>%s</td>\n",
					currencyLogic.formatHtml (
						chatUser.getChat ().getCurrency (),
						chatUserCredit.getCreditAmount ()));

				printFormat (
					"<td>%s</td>\n",
					currencyLogic.formatHtml (
						chatUser.getChat ().getCurrency (),
						chatUserCredit.getBillAmount ()));

				printFormat (
					"<td>%h</td>\n",
					 chatUserCredit.getDetails ());

				printFormat (
					"%s\n",
					consoleObjectManager.tdForObjectMiniLink (
						chatUserCredit.getUser ()));

				printFormat (
					"</tr>\n");

			}

		}

		printFormat (
			"</table>\n");

	}

}
