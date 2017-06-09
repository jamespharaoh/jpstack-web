package wbs.imchat.logic;

import static wbs.utils.etc.LogicUtils.referenceNotEqualWithClass;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.string.StringUtils.stringFormat;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.platform.currency.logic.CurrencyLogic;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.model.UserRec;

import wbs.utils.email.EmailLogic;
import wbs.utils.random.RandomLogic;
import wbs.utils.string.LazyFormatWriter;
import wbs.utils.time.TimeFormatter;

import wbs.imchat.model.ImChatConversationRec;
import wbs.imchat.model.ImChatCustomerRec;
import wbs.imchat.model.ImChatMessageRec;
import wbs.imchat.model.ImChatProfileRec;
import wbs.imchat.model.ImChatRec;

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

	@ClassSingletonDependency
	LogContext logContext;

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
			@NonNull Transaction parentTransactions,
			@NonNull ImChatConversationRec conversation) {

		try (

			NestedTransaction transaction =
				parentTransactions.nestTransaction (
					logContext,
					"conversationEnd");

		) {

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

	}

	@Override
	public
	void conversationEmailSend (
			@NonNull Transaction parentTransaction,
			@NonNull ImChatConversationRec conversation) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"conversationEmailSend");

		) {

			ImChatProfileRec profile =
				conversation.getImChatProfile ();

			ImChatCustomerRec customer =
				conversation.getImChatCustomer ();

			ImChatRec imChat =
				customer.getImChat ();

			// construct email content

			try (

				LazyFormatWriter formatWriter =
					new LazyFormatWriter ();

			) {

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
					formatWriter.toString ());

			}

		}

	}

	@Override
	public
	void customerPasswordGenerate (
			@NonNull Transaction parentTransaction,
			@NonNull ImChatCustomerRec customer,
			@NonNull Optional <UserRec> consoleUser) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"customerPasswordGenerate");

		) {

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
				optionalIsPresent (
					consoleUser)
			) {

				eventLogic.createEvent (
					transaction,
					"im_chat_customer_generated_password_from_console",
					optionalGetRequired (
						consoleUser),
					customer);

			} else {

				eventLogic.createEvent (
					transaction,
					"im_chat_customer_forgotten_password",
					customer);

			}

		}

	}

}
