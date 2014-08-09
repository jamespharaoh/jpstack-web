package wbs.apn.chat.tv.core.logic;

import wbs.apn.chat.core.logic.ChatLogicHooks;

public
class ChatTvChatHooks
	extends ChatLogicHooks.Abstract {

	/*

	@Inject
	ChatTvDao chatTvDao;

	@Inject
	ChatTvLogic chatTvLogic;

	@Inject
	QueueLogic queueLogic;

	@Override
	public
	void chatUserSignupComplete (
			ChatUserRec chatUser) {

		// find chat
		ChatRec chat =
			chatUser.getChat ();

		// find tv message
		ChatTvMessageRec tvMessage =
			chatTvDao.findMessageForSignup (chatUser.getId ());

		// do nothing if there is none
		if (tvMessage == null)
			return;

		// update status
		tvMessage.setStatus (ChatTvMessageStatus.moderating);

		// create chat tv moderation
		ChatTvModerationRec moderation =
			chatTvDao.findModerationById (chatUser.getId ());

		if (moderation != null) {

			// update message count
			moderation.setMessageCount (
				moderation.getMessageCount () + 1);

		} else {

			// find queue

			QueueRec queue =
				queueLogic.findQueue (chat, "tv_to_screen");

			// create queue item

			QueueItemRec queueItem =
				queueLogic.createQueueItem (
					queue,
					chatUser,
					chatUser,
					chatUser.getPrettyName (),
					tvMessage.getOriginalText ().getText ());

			// create moderation
			moderation = new ChatTvModerationRec ();
			moderation.setChatUser (chatUser);
			moderation.setQueueItem (queueItem);
			moderation.setMessageCount (1);
			moderation.setMessage (tvMessage);
			chatTvDao.insertModeration (moderation);
		}
	}

	@Override
	public
	void collectChatUserCharges (
			ChatUserRec chatUser,
			List<ChatUserCharge> internal,
			List<ChatUserCharge> external) {

		ChatRec chat =
			chatUser.getChat ();

		ChatTvRec chatTv =
			chatTvLogic.chatTv (chat);

		if (chatTv == null)
			return;

		ChatTvUserRec chatTvUser =
			chatTvLogic.chatTvUser (chatUser);

		ChatUserCharge textFree = new ChatUserCharge ();
		textFree.name = "TV to-screen text free";
		if (chatTvUser != null) {
			textFree.count = chatTvUser.getToScreenTextFree ();
			textFree.charge = 0;
		}
		internal.add (textFree);

		ChatUserCharge textCharge = new ChatUserCharge ();
		textCharge.name = "TV to-screen text bill";
		if (chatTvUser != null) {
			textCharge.count = chatTvUser.getToScreenTextCount ();
			textCharge.charge = chatTvUser.getToScreenTextCharge ();
		}
		internal.add (textCharge);

		ChatUserCharge photoFree = new ChatUserCharge ();
		photoFree.name = "TV to-screen photo free";
		if (chatTvUser != null) {
			photoFree.count = chatTvUser.getToScreenPhotoFree ();
			photoFree.charge = 0;
		}
		internal.add (photoFree);

		ChatUserCharge photoCharge = new ChatUserCharge ();
		photoCharge.name = "TV to-screen photo bill";
		if (chatTvUser != null) {
			photoCharge.count = chatTvUser.getToScreenPhotoCount ();
			photoCharge.charge = chatTvUser.getToScreenPhotoCharge ();
		}
		internal.add (photoCharge);

		ChatUserCharge legacyCharge = new ChatUserCharge ();
		legacyCharge.name = "TV to-screen (legacy)";
		if (chatTvUser != null) {
			legacyCharge.count = chatTvUser.getToScreenLegacyCount ();
			legacyCharge.charge = chatTvUser.getToScreenLegacyCharge ();
		}
		external.add (legacyCharge);
	}
	*/

}