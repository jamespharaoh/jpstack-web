package wbs.clients.apn.chat.tv.core.logic;

import wbs.framework.application.annotations.SingletonComponent;

@SingletonComponent ("chatTvLogic")
public
class ChatTvLogicImpl
	implements ChatTvLogic {

	/*
	@Inject
	ChatCreditLogic chatCreditLogic;

	@Inject
	ChatTvDao chatTvDao;

	@Inject
	ChatTvObjectHelper chatTvHelper;

	@Inject
	Database database;

	@Inject
	MediaObjectHelper mediaHelper;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	TextObjectHelper textHelper;

	@Override
	public ChatTvMessageRec postToScreen (
			UserRec user,
			ChatUserRec chatUser,
			String messageString,
			boolean postAsTextJockey) {

		Date now =
			new Date ();

		ChatTvRec chatTv =
			chatTvHelper.find (
				chatUser.getChat ().getId ());

		// process message text

		TextRec messageText =
			textHelper.findOrCreate (messageString);

		// create message

		ChatTvMessageRec message = new ChatTvMessageRec ();
		message.setChatTv (chatTv);
		message.setChatUser (chatUser);
		message.setTextJockey (postAsTextJockey);
		message.setOriginalText (messageText);
		message.setEditedText (messageText);
		message.setStatus (ChatTvMessageStatus.sending);
		message.setUser (user);
		message.setCreatedTime (now);
		message.setModeratedTime (now);
		chatTvDao.insertMessage (message);

		// create outbox

		ChatTvOutboxRec outbox = new ChatTvOutboxRec ();
		outbox.setMessage (message);
		outbox.setCreatedTime (now);
		chatTvDao.insertMessageOutbox (outbox);

		return message;
	}

	@Override
	public
	void markPicUploadedTx (
			int mediaId) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		MediaRec media =
			mediaHelper.find (
				mediaId);

		ChatTvPicUploadedRec picUploaded =
			new ChatTvPicUploadedRec ();

		picUploaded
			.setMedia (media);

		chatTvDao.insertChatTvPicUploaded (
			picUploaded);

		transaction.commit ();

	}

	@Override
	public
	void chatTvUserSpend (
			ChatTvUserRec chatTvUser,
			String countProperty,
			String chargeProperty,
			int countDelta,
			int chargeDelta) {

		ChatUserRec chatUser =
			chatTvUser.getChatUser ();

		LocalDate today =
			LocalDate.now ();

		// lookup spend

		ChatTvUserSpendRec chatTvUserSpend =
			findOrCreateChatTvUserSpend (chatTvUser, today);

		// perform updates

		incProperty (chatTvUser, countProperty, countDelta);
		incProperty (chatTvUser, chargeProperty, chargeDelta);

		incProperty (chatTvUserSpend, countProperty, countDelta);
		incProperty (chatTvUserSpend, chargeProperty, chargeDelta);

		// update the chat user

		if (chargeDelta > 0)
			chatCreditLogic.chatUserSpendBasic (
				chatUser,
				chargeDelta);

	}

	@Override
	public ChatTvUserSpendRec findOrCreateChatTvUserSpend (
			ChatTvUserRec chatTvUser,
			LocalDate date) {

		ChatTvUserSpendRec chatTvUserSpend =
			chatTvDao.findChatTvUserSpend (
				chatTvUser.getId (),
				date);

		if (chatTvUserSpend != null)
			return chatTvUserSpend;

		chatTvUserSpend = new ChatTvUserSpendRec ();
		chatTvUserSpend.setChatTvUser (chatTvUser);
		chatTvUserSpend.setDate (date);
		chatTvDao.insertChatTvUserSpend (chatTvUserSpend);

		return chatTvUserSpend;
	}

	public
	void incProperty (
			Object target,
			String property,
			int delta) {

		if (property == null)
			return;

		if (delta == 0)
			return;

		int value =
			(Integer)
			BeanLogic.getProperty (
				target,
				property);

		BeanLogic.setProperty (
			target,
			property,
			value + delta);

	}

	@Override
	public ChatTvUserRec chatTvUser (
			ChatUserRec chatUser) {

		ChatTvUserRec chatTvUser =
			chatTvDao.findChatTvUserById (
				chatUser.getId ());

		if (chatTvUser != null)
			return chatTvUser;

		chatTvUser =
			chatTvDao.insertChatTvUser (
				new ChatTvUserRec ()
					.setChatUser (chatUser));

		return chatTvUser;

	}

	@Override
	public
	ChatTvRec chatTv (
			ChatRec chat) {

		ChatTvRec chatTv =
			chatTvHelper.find (
				chat.getId ());

		return chatTv;
	}
	*/

}
