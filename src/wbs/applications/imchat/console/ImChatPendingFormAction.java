package wbs.applications.imchat.console;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.toInteger;

import javax.inject.Inject;

import lombok.Cleanup;

import wbs.applications.imchat.model.ImChatConversationRec;
import wbs.applications.imchat.model.ImChatCustomerRec;
import wbs.applications.imchat.model.ImChatMessageRec;
import wbs.applications.imchat.model.ImChatRec;
import wbs.applications.imchat.model.ImChatTemplateRec;
import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.currency.logic.CurrencyLogic;
import wbs.platform.queue.logic.QueueLogic;
import wbs.platform.user.console.UserConsoleLogic;

@PrototypeComponent ("imChatPendingFormAction")
public
class ImChatPendingFormAction
	extends ConsoleAction {

	// dependencies

	@Inject
	CurrencyLogic currencyLogic;

	@Inject
	Database database;

	@Inject
	ImChatMessageConsoleHelper imChatMessageHelper;

	@Inject
	ImChatTemplateConsoleHelper imChatTemplateHelper;

	@Inject
	QueueLogic queueLogic;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	UserConsoleLogic userConsoleLogic;

	// details

	@Override
	public
	Responder backupResponder () {

		return responder (
			"imChatPendingFormResponder");

	}

	// implementation

	@Override
	public
	Responder goReal () {

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ImChatPendingFormAction.goReal ()",
				this);

		// find message

		ImChatMessageRec customerMessage =
			imChatMessageHelper.findRequired (
				requestContext.stuffInt (
					"imChatMessageId"));

		ImChatConversationRec conversation =
			customerMessage.getImChatConversation ();

		ImChatCustomerRec customer =
			conversation.getImChatCustomer ();

		ImChatRec imChat =
			customer.getImChat ();

		// sanity check

		if (customerMessage.getQueueItem () == null)
			throw new RuntimeException ();

		// select template

		String templateString =
			requestContext.parameterRequired (
				"template");

		boolean bill;
		boolean ignore;

		long minLength;
		long maxLength;

		ImChatTemplateRec template;

		String messageText;

		if (
			equal (
				templateString,
				"bill")
		) {

			bill = true;
			ignore = false;
			template = null;

			messageText =
				requestContext.parameterRequired (
					"message-bill");

			minLength =
				imChat.getBillMessageMinChars ();

			maxLength =
				imChat.getBillMessageMaxChars ();

		} else if (
			equal (
				templateString,
				"free")
		) {

			bill = false;
			ignore = false;
			template = null;

			messageText =
				requestContext.parameterRequired (
					"message-free");

			minLength =
				imChat.getFreeMessageMinChars ();

			maxLength =
				imChat.getFreeMessageMaxChars ();

		} else if (
			equal (
				templateString,
				"ignore")
		) {

			bill = false;
			ignore = true;

			template = null;
			messageText = null;

			minLength = 0;
			maxLength = 0;

		} else {

			bill = false;
			ignore = false;

			template =
				imChatTemplateHelper.findRequired (
					toInteger (
						templateString));

			if (template.getImChat () != imChat)
				throw new RuntimeException ();

			messageText =
				template.getText ();

			minLength = 0;
			maxLength = Integer.MAX_VALUE;

		}

		// check message type

		if (
			bill
			&& customer.getBalance () < imChat.getMessageCost ()
		) {

			requestContext.addError (
				stringFormat (
					"Billed message price is %s, ",
					currencyLogic.formatText (
						imChat.getCreditCurrency (),
						imChat.getMessageCost ()),
					"but customer's balance is only %s",
					currencyLogic.formatText (
						imChat.getCreditCurrency (),
						customer.getBalance ())));

		}

		// check length

		if (! ignore) {

			int messageLength =
				messageText.length ();

			if (messageLength < minLength) {

				requestContext.addError (
					stringFormat (
						"Message has %s ",
						messageLength,
						"characters, but the minimum is %s, ",
						minLength,
						"please add %s more",
						minLength - messageLength));

				return null;

			}

			if (messageLength > maxLength) {

				requestContext.addError (
					stringFormat (
						"Message has %s ",
						messageLength,
						"characters, but the maximum is %s, ",
						maxLength,
						"please remove %s",
						messageLength - maxLength));

				return null;

			}

			// create reply

			ImChatMessageRec operatorMessage =
				imChatMessageHelper.insert (
					imChatMessageHelper.createInstance ()

				.setImChatConversation (
					conversation)

				.setIndex (
					(int) (long)
					conversation.getNumMessages ())

				.setSenderUser (
					userConsoleLogic.userRequired ())

				.setTimestamp (
					transaction.now ())

				.setMessageText (
					messageText)

				.setImChatTemplate (
					template)

				.setPartnerImChatMessage (
					customerMessage)

				.setPrice (
					bill
						? imChat.getMessageCost ()
						: null)

			);

			customerMessage

				.setPartnerImChatMessage (
					operatorMessage);

			// update conversation

			conversation

				.setNumMessages (
					conversation.getNumMessages () + 1)

				.setPendingReply (
					false);

			// update customer

			customer

				.setBalance (
					+ customer.getBalance ()
					- ifNull (
						operatorMessage.getPrice (),
						0l))

				.setTotalSpend (
					+ customer.getTotalSpend ()
					+ ifNull (
						operatorMessage.getPrice (),
						0l));

		} else {

			// update conversation

			conversation

				.setPendingReply (
					false);

		}

		// remove queue item

		queueLogic.processQueueItem (
			customerMessage.getQueueItem (),
			userConsoleLogic.userRequired ());

		// done

		transaction.commit ();

		if (ignore) {

			requestContext.addNotice (
				"Message ignored");

		} else {

			requestContext.addNotice (
				"Reply sent");

		}

		// return

		return responder (
			"queueHomeResponder");

	}


}
