package wbs.clients.apn.chat.tv.moderation.console;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.web.Responder;
import wbs.platform.console.action.ConsoleAction;

@PrototypeComponent ("chatTvModerationFormAction")
public
class ChatTvModerationFormAction
	extends ConsoleAction {

	@Override
	protected
	Responder backupResponder () {
		return null;
	}

	@Override
	protected
	Responder goReal () {
		return null;
	}

	/*
	@Inject
	ChatHelpLogLogic chatHelpLogLogic;

	@Inject
	ChatSendLogic chatSendLogic;

	@Inject
	ChatUserLogic chatUserLogic;

	@Inject
	ChatTvDao chatTvDao;

	@Inject
	ChatTvLogic chatTvLogic;

	@Inject
	ChatTvSchemeConsoleHelper chatTvSchemeHelper;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	Database database;

	@Inject
	MediaLogic mediaUtils;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	QueueLogic queueLogic;

	@Inject
	TextObjectHelper textHelper;

	@Inject
	UserObjectHelper userHelper;

	@Inject
	Provider<MessageSender> messageSender;

	@Override
	public Responder backupResponder () {
		return responder ("chatTvModerationFormResponder");
	}

	@Override
	protected
	Responder goReal () {

		String notice = null;

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		// find stuff

		UserRec myUser =
			userHelper.find (
				requestContext.userId ());

		ChatTvModerationRec moderation =
			chatTvDao.findModerationById (
				requestContext.stuffInt ("chatTvModerationId"));

		ChatTvMessageRec message =
			moderation.getMessage ();

		ChatTvRec chatTv =
			message.getChatTv ();

		QueueItemRec queueItem =
			moderation.getQueueItem ();

		ChatUserRec chatUser =
			moderation.getChatUser ();

		ChatTvUserRec chatTvUser =
			chatTvLogic.chatTvUser (chatUser);

		ChatSchemeRec chatScheme =
			chatUser.getChatScheme ();

		ChatTvSchemeRec chatTvScheme =
			chatTvSchemeHelper.find (
				chatScheme.getId ());

		ChatRec chat =
			chatUser.getChat ();

		MessageRec smsMessage =
			messageHelper.find (
				message.getThreadId ());

		boolean doSend =
			requestContext.parameter ("send") != null;

		boolean doRejectFree =
			requestContext.parameter ("rejectFree") != null;

		boolean doRejectBill =
			requestContext.parameter ("rejectBill") != null;

		// sanity checks

		if (message.getStatus () != ChatTvMessageStatus.moderating)
			throw new RuntimeException ();

		if (queueItem.getQueueItemClaim ().getUser () != myUser) {

			requestContext.addError ("This item is not in your queue");

			return responder ("queueHomeResponder");

		}

		if (chatUser.getNumber () == null) {
			notice = "Chat user has been deleted";
			doSend = false;
			doRejectFree = false;
			doRejectBill = false;
		}

		if (doSend) {

			// create text

			TextRec editedText =
				textHelper.findOrCreate (
					requestContext.parameter ("text"));

			// work out billing

			LocalDate today =
				LocalDate.fromDateFields (transaction.timestamp ());

			ChatTvUserSpendRec spend =
				chatTvLogic.findOrCreateChatTvUserSpend (
					chatTvUser,
					today);

			int dailyMax =
				message.getMedia () != null
					? chatTv.getToScreenPhotoDailyMax ()
					: chatTv.getToScreenTextDailyMax ();

			int freeCount =
				message.getMedia () != null
					? spend.getToScreenPhotoFree ()
					: spend.getToScreenTextFree ();

			int charge =
				message.getMedia () != null
					? chatTvScheme.getToScreenPhotoCharge ()
					: chatTvScheme.getToScreenTextCharge ();

			boolean free =
				freeCount < dailyMax;

			String countProperty =
				sf ("toScreen%s%s",
					message.getMedia () != null ? "Photo" : "Text",
					free ? "Free" : "Count");

			String chargeProperty =
				free ? null :
					sf ("toScreen%sCharge",
						message.getMedia () != null ? "Photo" : "Text");

			chatTvLogic.chatTvUserSpend (
				chatTvUser,
				countProperty,
				chargeProperty,
				1,
				free ? 0 : charge);

			// update message

			message.setEditedText (editedText);
			message.setModeratedTime (transaction.timestamp ());
			message.setStatus (
				chatTv.getToScreenMedia ()
					|| message.getMedia () == null ?
						ChatTvMessageStatus.sending
						: ChatTvMessageStatus.approved);
			message.setUser (myUser);

			// rotate image

			String orient = requestContext.parameter ("orient");
			if (orient != null && ! equal (orient, "up")) {

				MediaRec oldMedia =
					message.getMedia ();

				BufferedImage oldImage =
					mediaUtils.readImage (
						oldMedia.getContent ().getData (),
						oldMedia.getMediaType ().getMimeType ());

				BufferedImage newImage;
				if (equal (orient, "left"))
					newImage = mediaUtils.rotateImage90 (oldImage);
				else if (equal (orient, "right"))
					newImage = mediaUtils.rotateImage270 (oldImage);
				else if (equal (orient, "down"))
					newImage = mediaUtils.rotateImage180 (oldImage);
				else
					throw new RuntimeException ();

				MediaRec newMedia =
					mediaUtils.createImageMedia (
						newImage,
						oldMedia.getMediaType ().getMimeType (),
						oldMedia.getFilename ());

				message.setMedia (newMedia);
			}

			// create outbox

			if (message.getStatus () == ChatTvMessageStatus.sending) {
				ChatTvOutboxRec outbox = new ChatTvOutboxRec ();
				outbox.setMessage (message);
				outbox.setCreatedTime (transaction.timestamp ());
				chatTvDao.insertMessageOutbox (outbox);
			}

			// send confirmation message

			String templateName =
				sf ("to_screen_%s_%s",
					message.getMedia () != null ? "photo" : "text",
					free ? "free" : "bill");

			chatSendLogic.sendSystem (
				chatUser,
				message.getThreadId (),
				templateName,
				message.getMedia () != null
					? chatScheme.getRbFreeRouter ().getRoute ()
					: chatScheme.getMagicRouter ().getRoute (),
				smsMessage.getNumTo (),
				null,
				null,
				"bill",
				! free ? null :
					ImmutableMap.<String,String>builder ()
						.put ("num", Integer.toString (freeCount + 1))
						.put ("total", Integer.toString (dailyMax))
						.build ());

			// set image

			if (message.getMedia () != null) {

				chatUserLogic.setPhoto (
					chatUser,
					message.getMedia (),
					messageHelper.find (message.getThreadId ()),
					false);
			}

			notice = "Message sent";
		}

		if (doRejectFree || doRejectBill) {

			// send rejection message

			MessageRec sms =
				messageSender.get ()
					.threadId (message.getThreadId ())
					.number (chatUser.getNumber ())
					.messageString (requestContext.parameter ("message"))
					.numFrom (smsMessage.getNumTo ())
					.route (chatScheme.getRbFreeRouter ().getRoute ())
					.service (serviceHelper.findExistingByCode (chat, "system"))
					.send ();

			// log rejection message

			chatHelpLogLogic.createChatHelpLogOut (
				chatUser,
				null,
				myUser,
				sms,
				null,
				requestContext.parameter ("message"),
				null);

			// update message

			message.setModeratedTime (transaction.timestamp ());
			message.setStatus (ChatTvMessageStatus.rejected);
			message.setUser (myUser);

			notice = "Message rejected";
		}

		if (doRejectBill) {

			// bill the user for being naughty

			int charge =
				message.getMedia () != null
					? chatTvScheme.getToScreenPhotoCharge ()
					: chatTvScheme.getToScreenTextCharge ();

			String countProperty =
				sf ("toScreen%sCount",
					message.getMedia () != null ? "Photo" : "Text");

			String chargeProperty =
				sf ("toScreen%sCharge",
					message.getMedia () != null ? "Photo" : "Text");

			chatTvLogic.chatTvUserSpend (
				chatTvUser,
				countProperty,
				chargeProperty,
				1,
				charge);
		}

		if (moderation.getMessageCount () == 1) {

			// remove moderation
			chatTvDao.removeModeration (moderation);

			// process queue item

			queueLogic.processQueueItem (
				queueItem,
				myUser);

			// commit transaction

			transaction.commit ();

			// go to the reply page

			requestContext.addNotice (sf ("%s", notice));

			return responder ("chatTvModerationReplyResponder");

		} else {

			// find next message

			message =
				chatTvDao.findMessageForModeration (
					chatUser.getId ());

			// update moderation

			moderation
				.setMessage (message)
				.setMessageCount (moderation.getMessageCount () - 1);

			// update queue item

			queueItem
				.setDetails (message.getOriginalText ().getText ());

			// commit transaction
			transaction.commit ();

			// return to the same page
			requestContext.addNotice (
				sf ("%s, %d remaining",
					notice,
					moderation.getMessageCount ()));

			return responder ("chatTvModerationReplyResponder");

		}

	}
	*/

}