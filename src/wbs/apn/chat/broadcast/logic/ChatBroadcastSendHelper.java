package wbs.apn.chat.broadcast.logic;

import static wbs.utils.etc.EnumUtils.enumEqualSafe;
import static wbs.utils.etc.Misc.requiredValue;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;

import lombok.NonNull;

import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.object.ObjectHelper;

import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.send.GenericSendHelper;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;

import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.magicnumber.logic.MagicNumberLogic;
import wbs.sms.message.batch.model.BatchObjectHelper;
import wbs.sms.message.batch.model.BatchRec;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.number.lookup.logic.NumberLookupManager;

import wbs.apn.chat.bill.logic.ChatCreditLogic;
import wbs.apn.chat.broadcast.model.ChatBroadcastNumberObjectHelper;
import wbs.apn.chat.broadcast.model.ChatBroadcastNumberRec;
import wbs.apn.chat.broadcast.model.ChatBroadcastNumberState;
import wbs.apn.chat.broadcast.model.ChatBroadcastObjectHelper;
import wbs.apn.chat.broadcast.model.ChatBroadcastRec;
import wbs.apn.chat.broadcast.model.ChatBroadcastState;
import wbs.apn.chat.contact.model.ChatMessageObjectHelper;
import wbs.apn.chat.contact.model.ChatMessageStatus;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.scheme.model.ChatSchemeRec;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserRec;

@SingletonComponent ("chatBroadcastSendHelper")
public
class ChatBroadcastSendHelper
	implements
		GenericSendHelper <
			ChatRec,
			ChatBroadcastRec,
			ChatBroadcastNumberRec
		> {

	// singleton dependencies

	@SingletonDependency
	BatchObjectHelper batchHelper;

	@SingletonDependency
	ChatBroadcastObjectHelper chatBroadcastHelper;

	@SingletonDependency
	ChatBroadcastLogic chatBroadcastLogic;

	@SingletonDependency
	ChatBroadcastNumberObjectHelper chatBroadcastNumberHelper;

	@SingletonDependency
	ChatCreditLogic chatCreditLogic;

	@SingletonDependency
	ChatMessageObjectHelper chatMessageHelper;

	@SingletonDependency
	ChatUserLogic chatUserLogic;

	@SingletonDependency
	CommandObjectHelper commandHelper;

	@SingletonDependency
	Database database;

	@SingletonDependency
	EventLogic eventLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MagicNumberLogic magicNumberLogic;

	@SingletonDependency
	NumberLookupManager numberLookupManager;

	@SingletonDependency
	ServiceObjectHelper serviceHelper;

	// details

	@Override
	public
	ObjectHelper<ChatBroadcastNumberRec> itemHelper () {
		return chatBroadcastNumberHelper;
	}

	@Override
	public
	String itemNamePlural () {
		return "chat broadcasts";
	}

	@Override
	public
	ObjectHelper <ChatBroadcastRec> jobHelper () {
		return chatBroadcastHelper;
	}

	@Override
	public
	String name () {
		return "chat broadcast";
	}

	@Override
	public
	String parentTypeName () {
		return "chat-broadcast";
	}

	// implementation

	@Override
	public
	List <ChatBroadcastRec> findSendingJobs (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findSendingJobs");

		) {

			return chatBroadcastHelper.findSending (
				transaction);

		}

	}

	@Override
	public
	List <ChatBroadcastRec> findScheduledJobs (
			@NonNull Transaction parentTransaction,
			@NonNull Instant now) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findScheduledJobs");

		) {

			return chatBroadcastHelper.findScheduled (
				transaction,
				now);

		}

	}

	@Override
	public
	List <ChatBroadcastNumberRec> findItemsLimit (
			@NonNull Transaction parentTransaction,
			@NonNull ChatRec chat,
			@NonNull ChatBroadcastRec chatBroadcast,
			@NonNull Long maxResults) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findItemsLimit");

		) {

			return chatBroadcastNumberHelper.findAcceptedLimit (
				transaction,
				chatBroadcast,
				maxResults);

		}

	}

	@Override
	public
	ChatRec getService (
			@NonNull Transaction parentTransaction,
			@NonNull ChatBroadcastRec chatBroadcast) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getService");

		) {

			return chatBroadcast.getChat ();

		}

	}

	@Override
	public
	Instant getScheduledTime (
			@NonNull Transaction parentTransaction,
			@NonNull ChatRec chat,
			@NonNull ChatBroadcastRec chatBroadcast) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getScheduledTime");

		) {

			return chatBroadcast.getScheduledTime ();

		}

	}

	@Override
	public
	boolean jobScheduled (
			@NonNull Transaction parentTransaction,
			@NonNull ChatRec chat,
			@NonNull ChatBroadcastRec chatBroadcast) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"jobScheduled");

		) {

			return enumEqualSafe (
				chatBroadcast.getState (),
				ChatBroadcastState.scheduled);

		}

	}


	@Override
	public
	boolean jobSending (
			@NonNull Transaction parentTransaction,
			@NonNull ChatRec chat,
			@NonNull ChatBroadcastRec chatBroadcast) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"jobSending");

		) {

			return enumEqualSafe (
				chatBroadcast.getState (),
				ChatBroadcastState.sending);

		}

	}

	@Override
	public
	boolean jobConfigured (
			@NonNull Transaction parentTransaction,
			@NonNull ChatRec chat,
			@NonNull ChatBroadcastRec chatBroadcast) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"jobConfigured");

		) {

			// TODO something useful here

			return true;

		}

	}

	@Override
	public
	void sendStart (
			@NonNull Transaction parentTransaction,
			@NonNull ChatRec chat,
			@NonNull ChatBroadcastRec chatBroadcast) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"sendStart");

		) {

			// sanity check

			if (
				chatBroadcast.getState ()
					!= ChatBroadcastState.scheduled
			) {
				throw new IllegalStateException ();
			}

			// update broadcast

			chatBroadcast

				.setState (
					ChatBroadcastState.sending);

			chat

				.setNumChatBroadcastScheduled (
					chat.getNumChatBroadcastScheduled () - 1)

				.setNumChatBroadcastSending (
					chat.getNumChatBroadcastSending () + 1);

			// create event

			eventLogic.createEvent (
				transaction,
				"chat_broadcast_send_begun",
				chatBroadcast);

		}

	}

	@Override
	public
	boolean verifyItem (
			@NonNull Transaction parentTransaction,
			@NonNull ChatRec chat,
			@NonNull ChatBroadcastRec chatBroadcast,
			@NonNull ChatBroadcastNumberRec chatBroadcastNumber) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"verifyItem");

		) {

			ChatUserRec chatUser =
				chatBroadcastNumber.getChatUser ();

			return chatBroadcastLogic.canSendToUser (
				transaction,
				chatUser,
				chatBroadcast.getIncludeBlocked (),
				chatBroadcast.getIncludeOptedOut ());

		}

	}

	@Override
	public
	void rejectItem (
			@NonNull Transaction parentTransaction,
			@NonNull ChatRec chat,
			@NonNull ChatBroadcastRec chatBroadcast,
			@NonNull ChatBroadcastNumberRec chatBroadcastNumber) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"rejectItem");

		) {

			// sanity check

			if (
				chatBroadcastNumber.getState ()
					!= ChatBroadcastNumberState.accepted
			) {
				throw new IllegalStateException ();
			}

			// update number

			chatBroadcastNumber

				.setState (
					ChatBroadcastNumberState.rejected);

			chatBroadcast

				.setNumAccepted (
					chatBroadcast.getNumAccepted () - 1)

				.setNumRejected (
					chatBroadcast.getNumRejected () + 1);

		}

	}

	@Override
	public
	void sendItem (
			@NonNull Transaction parentTransaction,
			@NonNull ChatRec chat,
			@NonNull ChatBroadcastRec chatBroadcast,
			@NonNull ChatBroadcastNumberRec chatBroadcastNumber) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"sendItem");

		) {

			// sanity check

			if (
				chatBroadcastNumber.getState ()
					!= ChatBroadcastNumberState.accepted
			) {
				throw new IllegalStateException ();
			}

			// misc stuff

			ChatUserRec fromChatUser =
				chatBroadcast.getChatUser ();

			ChatUserRec toChatUser =
				chatBroadcastNumber.getChatUser ();

			ChatSchemeRec chatScheme =
				toChatUser.getChatScheme ();

			ServiceRec broadcastService =
				requiredValue (
					serviceHelper.findByCodeRequired (
						transaction,
						chat,
						"broadcast"));

			BatchRec batch =
				requiredValue (
					batchHelper.findByCodeRequired (
						transaction,
						chatBroadcast,
						"broadcast"));

			AffiliateRec affiliate =
				requiredValue (
					chatUserLogic.getAffiliate (
						transaction,
						toChatUser));

			// send message

			MessageRec message =
				magicNumberLogic.sendMessage (
					transaction,
					chatScheme.getMagicNumberSet (),
					toChatUser.getNumber (),
					commandHelper.findByCodeRequired (
						transaction,
						chat,
						"chat"),
					fromChatUser.getId (),
					optionalAbsent (),
					chatBroadcast.getText (),
					chatScheme.getMagicRouter (),
					broadcastService,
					optionalOf (
						batch),
					affiliate,
					optionalOf (
						chatBroadcast.getSentUser ()));

			// create chat message

			chatMessageHelper.insert (
				transaction,
				chatMessageHelper.createInstance ()

				.setChat (
					chat)

				.setFromUser (
					fromChatUser)

				.setToUser (
					toChatUser)

				.setTimestamp (
					transaction.now ())

				.setOriginalText (
					chatBroadcast.getText ())

				.setEditedText (
					chatBroadcast.getText ())

				.setStatus (
					ChatMessageStatus.broadcast)

				.setSender (
					chatBroadcast.getSentUser ())

			);

			// mark the number as sent

			chatBroadcastNumber

				.setState (
					ChatBroadcastNumberState.sent)

				.setMessage (
					message);

			chatBroadcast

				.setNumAccepted (
					chatBroadcast.getNumAccepted () - 1)

				.setNumSent (
					chatBroadcast.getNumSent () + 1);

		}

	}

	@Override
	public
	void sendComplete (
			@NonNull Transaction parentTransaction,
			@NonNull ChatRec chat,
			@NonNull ChatBroadcastRec chatBroadcast) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"sendComplete");

		) {

			// sanity check

			if (chatBroadcast.getNumAccepted () != 0) {

				throw new IllegalStateException (
					stringFormat (
						"Unable to complete send to chat broadcast %s ",
						integerToDecimalString (
							chatBroadcast.getId ()),
						"with %s numbers accepted",
						integerToDecimalString (
							chatBroadcast.getNumAccepted ())));

			}

			// update broadcast

			chatBroadcast

				.setState (
					ChatBroadcastState.sent)

				.setSentTime (
					transaction.now ());

			// update chat

			chat

				.setNumChatBroadcastSending (
					chat.getNumChatBroadcastSending () - 1)

				.setNumChatBroadcastSent (
					chat.getNumChatBroadcastSent () + 1);

			// create event

			eventLogic.createEvent (
				transaction,
				"chat_broadcast_send_completed",
				chatBroadcast);

		}

	}

}
