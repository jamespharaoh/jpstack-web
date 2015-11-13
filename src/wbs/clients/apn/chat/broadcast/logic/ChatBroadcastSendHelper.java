package wbs.clients.apn.chat.broadcast.logic;

import static wbs.framework.utils.etc.Misc.instantToDate;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import org.joda.time.Instant;

import wbs.clients.apn.chat.bill.logic.ChatCreditLogic;
import wbs.clients.apn.chat.broadcast.model.ChatBroadcastNumberObjectHelper;
import wbs.clients.apn.chat.broadcast.model.ChatBroadcastNumberRec;
import wbs.clients.apn.chat.broadcast.model.ChatBroadcastNumberState;
import wbs.clients.apn.chat.broadcast.model.ChatBroadcastObjectHelper;
import wbs.clients.apn.chat.broadcast.model.ChatBroadcastRec;
import wbs.clients.apn.chat.broadcast.model.ChatBroadcastState;
import wbs.clients.apn.chat.contact.model.ChatMessageObjectHelper;
import wbs.clients.apn.chat.contact.model.ChatMessageStatus;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.scheme.model.ChatSchemeRec;
import wbs.clients.apn.chat.user.core.logic.ChatUserLogic;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
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
import wbs.sms.message.outbox.logic.MessageSender;
import wbs.sms.number.lookup.logic.NumberLookupManager;

@SingletonComponent ("chatBroadcastSendHelper")
public
class ChatBroadcastSendHelper
	implements
		GenericSendHelper<
			ChatRec,
			ChatBroadcastRec,
			ChatBroadcastNumberRec
		> {

	// dependencies

	@Inject
	BatchObjectHelper batchHelper;

	@Inject
	ChatBroadcastObjectHelper chatBroadcastHelper;

	@Inject
	ChatBroadcastLogic chatBroadcastLogic;

	@Inject
	ChatBroadcastNumberObjectHelper chatBroadcastNumberHelper;

	@Inject
	ChatCreditLogic chatCreditLogic;

	@Inject
	ChatMessageObjectHelper chatMessageHelper;

	@Inject
	ChatUserLogic chatUserLogic;

	@Inject
	CommandObjectHelper commandHelper;

	@Inject
	Database database;

	@Inject
	EventLogic eventLogic;

	@Inject
	MagicNumberLogic magicNumberLogic;

	@Inject
	NumberLookupManager numberLookupManager;

	@Inject
	ServiceObjectHelper serviceHelper;

	// prototype dependencies

	@Inject
	Provider<MessageSender> messageSenderProvider;

	// details

	@Override
	public
	String name () {
		return "chat broadcast";
	}

	@Override
	public
	String itemNamePlural () {
		return "chat broadcasts";
	}

	@Override
	public
	ObjectHelper<ChatBroadcastRec> jobHelper () {
		return chatBroadcastHelper;
	}

	@Override
	public
	ObjectHelper<ChatBroadcastNumberRec> itemHelper () {
		return chatBroadcastNumberHelper;
	}

	// implementation

	@Override
	public
	List<ChatBroadcastRec> findSendingJobs () {

		return chatBroadcastHelper.findSending ();

	}

	@Override
	public
	List<ChatBroadcastRec> findScheduledJobs (
			Instant now) {

		return chatBroadcastHelper.findScheduled (
			now);

	}

	@Override
	public
	List<ChatBroadcastNumberRec> findItemsLimit (
			ChatRec chat,
			ChatBroadcastRec chatBroadcast,
			int maxResults) {

		return chatBroadcastNumberHelper.findAcceptedLimit (
			chatBroadcast,
			maxResults);

	}

	@Override
	public
	ChatRec getService (
			ChatBroadcastRec chatBroadcast) {

		return chatBroadcast.getChat ();

	}

	@Override
	public
	Instant getScheduledTime (
			ChatRec chat,
			ChatBroadcastRec chatBroadcast) {

		return chatBroadcast.getScheduledTime ();

	}

	@Override
	public
	boolean jobScheduled (
			ChatRec chat,
			ChatBroadcastRec chatBroadcast) {

		return chatBroadcast.getState ()
			== ChatBroadcastState.scheduled;

	}


	@Override
	public
	boolean jobSending (
			ChatRec chat,
			ChatBroadcastRec chatBroadcast) {

		return chatBroadcast.getState ()
			== ChatBroadcastState.sending;

	}

	@Override
	public
	boolean jobConfigured (
			ChatRec chat,
			ChatBroadcastRec chatBroadcast) {

		// TODO something useful here

		return true;

	}

	@Override
	public
	void sendStart (
			ChatRec chat,
			ChatBroadcastRec chatBroadcast) {

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
			"chat_broadcast_send_begun",
			chatBroadcast);

	}

	@Override
	public
	boolean verifyItem (
			ChatRec chat,
			ChatBroadcastRec chatBroadcast,
			ChatBroadcastNumberRec chatBroadcastNumber) {

		ChatUserRec chatUser =
			chatBroadcastNumber.getChatUser ();

		return chatBroadcastLogic.canSendToUser (
			chatUser,
			chatBroadcast.getIncludeBlocked (),
			chatBroadcast.getIncludeOptedOut ());

	}

	@Override
	public
	void rejectItem (
			ChatRec chat,
			ChatBroadcastRec chatBroadcast,
			ChatBroadcastNumberRec chatBroadcastNumber) {

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
			ChatRec chat,
			ChatBroadcastRec chatBroadcast,
			ChatBroadcastNumberRec chatBroadcastNumber) {

		Transaction transaction =
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
			serviceHelper.findByCode (
				chat,
				"broadcast");

		BatchRec batch =
			batchHelper.findByCode (
				chatBroadcast,
				"broadcast");

		AffiliateRec affiliate =
			chatUserLogic.getAffiliate (
				toChatUser);

		// send message

		MessageRec message =
			magicNumberLogic.sendMessage (
				chatScheme.getMagicNumberSet (),
				toChatUser.getNumber (),
				commandHelper.findByCode (chat, "chat"),
				fromChatUser.getId (),
				null,
				chatBroadcast.getText (),
				chatScheme.getMagicRouter (),
				broadcastService,
				batch,
				affiliate);

		// create chat message

		chatMessageHelper.insert (
			chatMessageHelper.createInstance ()

			.setChat (
				chat)

			.setFromUser (
				fromChatUser)

			.setToUser (
				toChatUser)

			.setTimestamp (
				instantToDate (
					transaction.now ()))

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

	@Override
	public
	void sendComplete (
			ChatRec chat,
			ChatBroadcastRec chatBroadcast) {

		Transaction transaction =
			database.currentTransaction ();

		// sanity check

		if (chatBroadcast.getNumAccepted () != 0) {

			throw new IllegalStateException (
				stringFormat (
					"Unable to complete send to chat broadcast %s ",
					chatBroadcast.getId (),
					"with %s numbers accepted",
					chatBroadcast.getNumAccepted ()));

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
			"chat_broadcast_send_completed",
			chatBroadcast);

	}

}
