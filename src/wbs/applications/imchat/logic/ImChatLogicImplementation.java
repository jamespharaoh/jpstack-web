package wbs.applications.imchat.logic;

import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.notEqual;
import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;

import lombok.NonNull;

import org.apache.commons.io.output.StringBuilderWriter;

import com.google.common.collect.ImmutableList;

import wbs.applications.imchat.model.ImChatConversationRec;
import wbs.applications.imchat.model.ImChatCustomerRec;
import wbs.applications.imchat.model.ImChatMessageRec;
import wbs.applications.imchat.model.ImChatProfileRec;
import wbs.applications.imchat.model.ImChatRec;
import wbs.console.misc.TimeFormatter;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.utils.EmailLogic;
import wbs.framework.utils.etc.FormatWriter;
import wbs.framework.utils.etc.FormatWriterWriter;
import wbs.platform.currency.logic.CurrencyLogic;

@SingletonComponent ("imChatLogic")
public
class ImChatLogicImplementation
	implements ImChatLogic {

	// dependencies

	@Inject
	CurrencyLogic currencyLogic;

	@Inject
	Database database;

	@Inject
	EmailLogic emailLogic;

	@Inject
	TimeFormatter timeFormatter;

	// implementation

	@Override
	public
	void conversationEnd (
			@NonNull ImChatConversationRec conversation) {

		Transaction transaction =
			database.currentTransaction ();

		ImChatCustomerRec customer =
			conversation.getImChatCustomer ();

		// check preconditions

		if (
			notEqual (
				customer.getCurrentConversation (),
				conversation)
		) {

			throw new IllegalStateException ();

		}

		// update conversation

		conversation

			.setEndTime (
				transaction.now ());

		// update customer

		customer

			.setCurrentConversation (
				null);

	}

	@Override
	public
	void conversationEmailSend (
			@NonNull ImChatConversationRec conversation) {

		ImChatProfileRec profile =
			conversation.getImChatProfile ();

		ImChatCustomerRec customer =
			conversation.getImChatCustomer ();

		ImChatRec imChat =
			customer.getImChat ();

		// construct email content

		StringBuilder stringBuilder =
			new StringBuilder ();

		FormatWriter formatWriter =
			new FormatWriterWriter (
				new StringBuilderWriter (
					stringBuilder));

		formatWriter.writeFormat (
			"Thanks for using the psychic chat service. For your future ",
			"reference, we have included a transcript of your recent ",
			"conversation with %s.\n",
			profile.getPublicName ());

		formatWriter.writeFormat (
			"\n");

		for (
			ImChatMessageRec message
				: conversation.getMessages ()
		) {

			formatWriter.writeFormat (
				"%s %s:\n",
				timeFormatter.instantToTimeString (
					timeFormatter.defaultTimezone (),
					message.getTimestamp ()),
				message.getSenderUser () != null
					? profile.getPublicName ()
					: "Me");

			formatWriter.writeFormat (
				"%s\n",
				message.getMessageText ());

			if (
				isNotNull (
					message.getPrice ())
			) {

				formatWriter.writeFormat (
					"(you were charged %s for this message)\n",
					currencyLogic.formatText (
						imChat.getCurrency (),
						(long) message.getPrice ()));

			}

			formatWriter.writeFormat (
				"\n");

		}

		// send email

		emailLogic.sendEmail (
			imChat.getEmailFromName (),
			imChat.getEmailFromAddress (),
			imChat.getEmailReplyToAddress (),
			ImmutableList.<String>of (
				customer.getEmail ()),
			stringFormat (
				"Your recent conversation with %s",
				profile.getPublicName ()),
			stringBuilder.toString ());

	}

}
