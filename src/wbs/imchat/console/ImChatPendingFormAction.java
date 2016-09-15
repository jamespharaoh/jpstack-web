package wbs.imchat.console;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NumberUtils.maximumJavaInteger;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;

import lombok.Cleanup;

import wbs.imchat.console.ImChatMessageConsoleHelper;
import wbs.imchat.console.ImChatTemplateConsoleHelper;
import wbs.imchat.model.ImChatConversationRec;
import wbs.imchat.model.ImChatCustomerRec;
import wbs.imchat.model.ImChatMessageRec;
import wbs.imchat.model.ImChatRec;
import wbs.imchat.model.ImChatTemplateRec;
import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
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

	@SingletonDependency
	CurrencyLogic currencyLogic;

	@SingletonDependency
	Database database;

	@SingletonDependency
	ImChatMessageConsoleHelper imChatMessageHelper;

	@SingletonDependency
	ImChatTemplateConsoleHelper imChatTemplateHelper;

	@SingletonDependency
	QueueLogic queueLogic;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
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
				requestContext.stuffInteger (
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
			stringEqualSafe (
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
			stringEqualSafe (
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
			stringEqualSafe (
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
					Long.parseLong (
						templateString));

			if (template.getImChat () != imChat)
				throw new RuntimeException ();

			messageText =
				template.getText ();

			minLength = 0;
			maxLength = maximumJavaInteger;

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
