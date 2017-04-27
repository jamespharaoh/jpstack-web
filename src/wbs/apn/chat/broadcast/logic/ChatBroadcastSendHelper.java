package wbs.apn.chat.broadcast.logic;

import static wbs.utils.etc.Misc.requiredValue;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;

import javax.inject.Provider;

import lombok.NonNull;

import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.BorrowedTransaction;
import wbs.framework.database.Database;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;
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
import wbs.sms.message.outbox.logic.SmsMessageSender;
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

	// prototype dependencies

	@PrototypeDependency
	Provider <SmsMessageSender> messageSenderProvider;

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
	List<ChatBroadcastRec> findSendingJobs () {

		return chatBroadcastHelper.findSending ();

	}

	@Override
	public
	List <ChatBroadcastRec> findScheduledJobs (
			@NonNull Instant now) {

		return chatBroadcastHelper.findScheduled (
			now);

	}

	@Override
	public
	List <ChatBroadcastNumberRec> findItemsLimit (
			@NonNull ChatRec chat,
			@NonNull ChatBroadcastRec chatBroadcast,
			int maxResults) {

		return chatBroadcastNumberHelper.findAcceptedLimit (
			chatBroadcast,
			maxResults);

	}

	@Override
	public
	ChatRec getService (
			@NonNull ChatBroadcastRec chatBroadcast) {

		return chatBroadcast.getChat ();

	}

	@Override
	public
	Instant getScheduledTime (
			@NonNull ChatRec chat,
			@NonNull ChatBroadcastRec chatBroadcast) {

		return chatBroadcast.getScheduledTime ();

	}

	@Override
	public
	boolean jobScheduled (
			@NonNull ChatRec chat,
			@NonNull ChatBroadcastRec chatBroadcast) {

		return chatBroadcast.getState ()
			== ChatBroadcastState.scheduled;

	}


	@Override
	public
	boolean jobSending (
			@NonNull ChatRec chat,
			@NonNull ChatBroadcastRec chatBroadcast) {

		return chatBroadcast.getState ()
			== ChatBroadcastState.sending;

	}

	@Override
	public
	boolean jobConfigured (
			@NonNull ChatRec chat,
			@NonNull ChatBroadcastRec chatBroadcast) {

		// TODO something useful here

		return true;

	}

	@Override
	public
	void sendStart (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ChatRec chat,
			@NonNull ChatBroadcastRec chatBroadcast) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
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
				taskLogger,
				"chat_broadcast_send_begun",
				chatBroadcast);

		}

	}

	@Override
	public
	boolean verifyItem (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ChatRec chat,
			@NonNull ChatBroadcastRec chatBroadcast,
			@NonNull ChatBroadcastNumberRec chatBroadcastNumber) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"verifyItem");

		) {

			ChatUserRec chatUser =
				chatBroadcastNumber.getChatUser ();

			return chatBroadcastLogic.canSendToUser (
				taskLogger,
				chatUser,
				chatBroadcast.getIncludeBlocked (),
				chatBroadcast.getIncludeOptedOut ());

		}

	}

	@Override
	public
	void rejectItem (
			@NonNull ChatRec chat,
			@NonNull ChatBroadcastRec chatBroadcast,
			@NonNull ChatBroadcastNumberRec chatBroadcastNumber) {

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

	@Override
	public
	void sendItem (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ChatRec chat,
			@NonNull ChatBroadcastRec chatBroadcast,
			@NonNull ChatBroadcastNumberRec chatBroadcastNumber) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"sendItem");

		) {

			BorrowedTransaction transaction =
				database.currentTransaction ();

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
						chat,
						"broadcast"));

			BatchRec batch =
				requiredValue (
					batchHelper.findByCodeRequired (
						chatBroadcast,
						"broadcast"));

			AffiliateRec affiliate =
				requiredValue (
					chatUserLogic.getAffiliate (
						toChatUser));

			// send message

			MessageRec message =
				magicNumberLogic.sendMessage (
					taskLogger,
					chatScheme.getMagicNumberSet (),
					toChatUser.getNumber (),
					commandHelper.findByCodeRequired (
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
				taskLogger,
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
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ChatRec chat,
			@NonNull ChatBroadcastRec chatBroadcast) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"sendComplete");

		) {

			BorrowedTransaction transaction =
				database.currentTransaction ();

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
				taskLogger,
				"chat_broadcast_send_completed",
				chatBroadcast);

		}

	}

}
