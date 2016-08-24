package wbs.applications.imchat.logic;

import static wbs.framework.utils.etc.LogicUtils.referenceNotEqualWithClass;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.NullUtils.ifNull;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import org.apache.commons.io.output.StringBuilderWriter;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import lombok.NonNull;
import wbs.applications.imchat.model.ImChatConversationRec;
import wbs.applications.imchat.model.ImChatCustomerRec;
import wbs.applications.imchat.model.ImChatMessageRec;
import wbs.applications.imchat.model.ImChatProfileRec;
import wbs.applications.imchat.model.ImChatRec;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.application.annotations.SingletonDependency;
import wbs.framework.application.config.WbsConfig;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.utils.EmailLogic;
import wbs.framework.utils.RandomLogic;
import wbs.framework.utils.TimeFormatter;
import wbs.framework.utils.etc.FormatWriter;
import wbs.framework.utils.etc.FormatWriterWriter;
import wbs.platform.currency.logic.CurrencyLogic;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.model.UserRec;

@SingletonComponent ("imChatLogic")
public
class ImChatLogicImplementation
	implements ImChatLogic {

	// dependencies

	@SingletonDependency
	CurrencyLogic currencyLogic;

	@SingletonDependency
	Database database;

	@SingletonDependency
	EmailLogic emailLogic;

	@SingletonDependency
	EventLogic eventLogic;

	@SingletonDependency
	RandomLogic randomLogic;

	@SingletonDependency
	TimeFormatter timeFormatter;

	@SingletonDependency
	WbsConfig wbsConfig;

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
			referenceNotEqualWithClass (
				ImChatConversationRec.class,
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
				timeFormatter.timeString (
					timeFormatter.timezone (
						ifNull (
							imChat.getSlice ().getDefaultTimezone (),
							wbsConfig.defaultTimezone ())),
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
						imChat.getCreditCurrency (),
						message.getPrice ()));

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

	@Override
	public
	void customerPasswordGenerate (
			@NonNull ImChatCustomerRec customer,
			@NonNull Optional<UserRec> consoleUser) {

		ImChatRec imChat =
			customer.getImChat ();

		// generate new password

		String newPassword =
			randomLogic.generateLowercase (12);

		// update customer password

		customer

			.setPassword (
				newPassword);

		// send new password via mail

		emailLogic.sendEmail (
			imChat.getEmailFromName (),
			imChat.getEmailFromAddress (),
			imChat.getEmailReplyToAddress (),
			ImmutableList.of (
				customer.getEmail ()),
			imChat.getEmailSubjectForgotPassword (),
			stringFormat (
				"Please log on with your new password:\n",
				"\n",
				"%s\n",
				newPassword));

		// create log

		if (
			isNotNull (
				consoleUser)
		) {

			eventLogic.createEvent (
				"im_chat_customer_generated_password_from_console",
				consoleUser.get (),
				customer);

		} else {

			eventLogic.createEvent (
				"im_chat_customer_forgotten_password",
				customer);

		}

	}

}
