package wbs.apn.chat.tv.core.daemon;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.daemon.ReceivedMessage;

@PrototypeComponent ("chatTvToScreenCommand")
public
class ChatTvToScreenCommand
	implements CommandHandler {

	/*
	@Inject
	ChatCreditLogic chatCreditLogic;

	@Inject
	ChatMiscLogic chatMiscLogic;

	@Inject
	ChatSchemeObjectHelper chatSchemeHelper;

	@Inject
	ChatSendLogic chatSendLogic;

	@Inject
	ChatTvObjectHelper chatTvHelper;

	@Inject
	ChatUserLogic chatUserLogic;

	@Inject
	CommandObjectHelper commandHelper;

	@Inject
	CommandLogic commandLogic;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	Database database;

	@Inject
	MediaLogic mediaUtils;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	ObjectManager objectManager;

	@Inject
	QueueLogic queueLogic;

	@Inject
	TextObjectHelper textHelper;
	*/

	@Override
	public String[] getCommandTypes () {

		return new String [] {
			"chat.tv_to_screen"
		};

	}

	@Override
	public
	void handle (
			int commandId,
			ReceivedMessage receivedMessage) {

/*
		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		MessageRec smsMessage =
			messageHelper.find (
				receivedMessage.getMessageId ());

		CommandRec command =
			commandHelper.find (
				commandId);

		ChatTvRec chatTv =
			chatTvHelper.find (
				command.getParentObjectId ());

		if (chatTv == null) {

			log.warn (sf ("%s: %s",
				sf ("Unable to process message %d",
					receivedMessage.getMessageId ()),
				sf ("No chat TV record found with id %d",
					command.getParentObjectId ())));

			return Status.notprocessed;

		}

		ChatRec chat =
			chatTv.getChat ();

		ChatUserRec chatUser =
			chatUserLogic.findOrCreateByNumber (
				chat,
				smsMessage);

		ChatSchemeRec chatScheme =
			chatUser.getChatScheme ();

		// set received message stuff

		chatUserLogic.setAffiliateId (
			receivedMessage,
			chatUser);

		chatMiscLogic.setServiceId (
			receivedMessage,
			chat,
			"tv_to_screen");

		// ignore barred users

		if (! chatCreditLogic.userSpendCheck (
				chatUser,
				true,
				smsMessage.getThreadId (),
				false))

			return CommandHandler.Status.processed;

		// set chat scheme

		if (chatUser.getChatScheme () == null) {

			chatScheme =
				chatSchemeHelper.findByCode (
					objectManager.getGlobalId (chat),
					"uk");

			chatUser
				.setChatScheme (chatScheme);

		}

		TextRec text =
			textHelper.findOrCreate (
				receivedMessage.getRest ());

		// create chat tv message

		ChatTvMessageRec tvMessage =
			new ChatTvMessageRec ()
				.setChatTv (chatTv)
				.setChatUser (chatUser)
				.setTextJockey (false)
				.setOriginalText (text)
				.setCreatedTime (transaction.timestamp ())
				.setThreadId (smsMessage.getThreadId ());

		// set the media

		MediaRec oldMedia =
			chatUserLogic.findPhoto (smsMessage);

		if (oldMedia != null) {

			BufferedImage image =
				mediaUtils.readImage (
					oldMedia.getContent ().getData (),
					oldMedia.getMediaType ().getMimeType ());

			MediaRec newMedia =
				mediaUtils.createImageMedia (
					image,
					oldMedia.getMediaType ().getMimeType (),
					oldMedia.getFilename ());

			tvMessage.setMedia (newMedia);
		}

		// if the user has not signed up

		if (chatUser.getFirstJoin () == null) {

			// cancel previous tv message

			ChatTvMessageRec oldMessage =
				chatTvDao.findMessageForSignup (
					chatUser.getId ());

			if (oldMessage != null) {
				oldMessage.setStatus (ChatTvMessageStatus.replaced);
			}

			// insert tv message

			tvMessage.setStatus (ChatTvMessageStatus.signup);
			chatTvDao.insertMessage (tvMessage);

			// set user gender and orient

			chatUser.setGender (Gender.male);
			chatUser.setOrient (Orient.gay);

			// send informational message

			chatSendLogic.sendSystem (
				chatUser,
				smsMessage.getThreadId (),
				"to_screen_signup",
				chatScheme.getRbFreeRouter ().getRoute (),
				smsMessage.getNumTo (),
				null,
				null,
				"system",
				null);

			// send signup message

			chatSendLogic.sendSystemMagic (
				chatUser,
				smsMessage.getThreadId (),
				"dob_request",
				commandLogic.findCommand (chat, "magic"),
				commandLogic.findCommand (chatScheme, "chat_dob").getId (),
				null);

		} else {

			// insert tv message

			tvMessage.setStatus (ChatTvMessageStatus.moderating);
			chatTvDao.insertMessage (tvMessage);

			// create chat tv moderation

			ChatTvModerationRec moderation =
				chatTvDao.findModerationById (chatUser.getId ());

			if (moderation != null) {

				moderation.setMessageCount (
					moderation.getMessageCount () + 1);

			} else {

				QueueRec queue =
					queueLogic.findQueue (
						chat,
						"tv_to_screen");

				QueueItemRec queueItem =
					queueLogic.createQueueItem (
						queue,
						chatUser,
						chatUser,
						chatUser.getPrettyName (),
						text.getText ());

				moderation = new ChatTvModerationRec ();
				moderation.setChatUser (chatUser);
				moderation.setQueueItem (queueItem);
				moderation.setMessageCount (1);
				moderation.setMessage (tvMessage);
				chatTvDao.insertModeration (moderation);
			}

			// auto join chat & date

			chatMiscLogic.userAutoJoin (
				chatUser,
				smsMessage);

		}

		// commit transaction

		transaction.commit ();

		return Status.processed;
		*/

throw new RuntimeException ();

	}

}