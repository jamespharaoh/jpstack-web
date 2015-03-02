package wbs.clients.apn.chat.user.pending.console;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.in;
import static wbs.framework.utils.etc.Misc.instantToDate;
import static wbs.framework.utils.etc.Misc.notEqual;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import lombok.Cleanup;
import wbs.clients.apn.chat.contact.logic.ChatMessageLogic;
import wbs.clients.apn.chat.contact.logic.ChatSendLogic;
import wbs.clients.apn.chat.contact.model.ChatMessageMethod;
import wbs.clients.apn.chat.contact.model.ChatMessageRec;
import wbs.clients.apn.chat.contact.model.ChatMessageStatus;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.help.logic.ChatHelpLogLogic;
import wbs.clients.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.clients.apn.chat.user.core.logic.ChatUserLogic;
import wbs.clients.apn.chat.user.core.logic.ChatUserLogic.PendingMode;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.image.model.ChatUserImageRec;
import wbs.clients.apn.chat.user.image.model.ChatUserImageType;
import wbs.clients.apn.chat.user.info.model.ChatUserInfoRec;
import wbs.clients.apn.chat.user.info.model.ChatUserInfoStatus;
import wbs.clients.apn.chat.user.info.model.ChatUserNameRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.utils.etc.BeanLogic;
import wbs.framework.web.Responder;
import wbs.platform.console.action.ConsoleAction;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.queue.logic.QueueLogic;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextRec;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.gsm.Gsm;
import wbs.sms.message.core.model.MessageRec;

import com.google.common.base.Optional;

@PrototypeComponent ("chatUserPendingFormAction")
public
class ChatUserPendingFormAction
	extends ConsoleAction {

	@Inject
	ChatHelpLogLogic chatHelpLogLogic;

	@Inject
	ChatSendLogic chatSendLogic;

	@Inject
	ChatMessageLogic chatMessageLogic;

	@Inject
	ChatUserConsoleHelper chatUserHelper;

	@Inject
	ChatUserLogic chatUserLogic;

	@Inject
	CommandObjectHelper commandHelper;

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	Database database;

	@Inject
	QueueLogic queueLogic;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	TextObjectHelper textHelper;

	@Inject
	UserObjectHelper userHelper;

	@Override
	protected
	Responder backupResponder () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		ChatUserRec chatUser =
			chatUserHelper.find (
				requestContext.stuffInt ("chatUserId"));

		return nextResponder (
			chatUser);

	}

	@Override
	protected
	Responder goReal () {

		// delegate appropriately

		if (requestContext.parameter ("unqueue") != null)
			return goUnqueue ();

		if (requestContext.parameter ("chatUserNameApprove") != null)
			return goApproveName ();

		if (requestContext.parameter ("chatUserInfoApprove") != null)
			return goApproveInfo ();

		if (requestContext.parameter ("chatUserImageApprove") != null)
			return goApproveImage (PendingMode.image);

		if (requestContext.parameter ("chatUserVideoApprove") != null)
			return goApproveImage (PendingMode.video);

		if (requestContext.parameter ("chatUserAudioApprove") != null)
			return goApproveImage (PendingMode.audio);

		if (requestContext.parameter("chatUserNameReject") != null)
			return goRejectName ();

		if (requestContext.parameter("chatUserInfoReject") != null)
			return goRejectInfo ();

		if (requestContext.parameter("chatUserImageReject") != null)
			return goRejectImage (PendingMode.image);

		if (requestContext.parameter ("chatUserVideoReject") != null)
			return goRejectImage (PendingMode.video);

		if (requestContext.parameter ("chatUserAudioReject") != null)
			return goRejectImage (PendingMode.audio);

		throw new RuntimeException("Invalid parameters");

	}

	private
	Responder goUnqueue () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		ChatUserRec chatUser =
			chatUserHelper.find (
				requestContext.stuffInt ("chatUserId"));

		UserRec myUser =
			userHelper.find (
				requestContext.userId ());

		Responder responder =
			updateQueueItem (
				chatUser,
				myUser);

		transaction.commit ();

		return responder;

	}

	private
	Responder goApproveName () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		// get database objects

		ChatUserRec chatUser =
			chatUserHelper.find (
				requestContext.stuffInt ("chatUserId"));

		UserRec myUser =
			userHelper.find (
				requestContext.userId ());

		// confirm there is something to approve

		if (chatUser.getNewChatUserName () == null) {

			requestContext.addError (
				"No name to approve");

			return nextResponder (
				chatUser);

		}

		// update the chat user and stuff

		ChatUserNameRec chatUserName =
			chatUser.getNewChatUserName ();

		if (
			notEqual (
				chatUserName.getStatus (),
				ChatUserInfoStatus.moderatorPending)
		) {

			throw new RuntimeException ();

		}

		chatUserName

			.setModerator (
				myUser)

			.setStatus (
				equal (
						requestContext.parameter ("name"),
						chatUserName.getOriginalName ())
					? ChatUserInfoStatus.moderatorApproved
					: ChatUserInfoStatus.moderatorEdited)

			.setModerationTime (
				instantToDate (
					transaction.now ()))

			.setEditedName (
				requestContext.parameter ("name"));

		chatUser

			.setName (
				chatUserName.getEditedName ())

			.setNewChatUserName (
				null);

		// remove the queue item and create any new one

		Responder responder =
			updateQueueItem (
				chatUser,
				myUser);

		transaction.commit ();

		// add a notice

		requestContext.addNotice (
			"Chat user name approved");

		// and return

		return responder;

	}

	private
	Responder goApproveInfo () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		// get database objects

		ChatUserRec chatUser =
			chatUserHelper.find (
				requestContext.stuffInt ("chatUserId"));

		UserRec myUser =
			userHelper.find (
				requestContext.userId ());

		// confirm there is something to approve

		if (chatUser.getNewChatUserInfo() == null) {

			requestContext.addError (
				"No info to approve");

			return nextResponder (
				chatUser);

		}

		// update the chat user and stuff

		ChatUserInfoRec chatUserInfo =
			chatUser.getNewChatUserInfo ();

		if (
			notEqual (
				chatUserInfo.getStatus (),
				ChatUserInfoStatus.moderatorPending)
		) {

			throw new RuntimeException ();

		}

		chatUserInfo

			.setModerator (
				myUser)

			.setStatus (
				equal (
						requestContext.parameter ("info"),
						chatUserInfo.getOriginalText ().getText ())
					? ChatUserInfoStatus.moderatorApproved
					: ChatUserInfoStatus.moderatorEdited)

			.setModerationTime (
				new Date ())

			.setEditedText (
				textHelper.findOrCreate (
					requestContext.parameter ("info")));

		chatUser

			.setInfoText (
				chatUserInfo.getEditedText ())

			.setNewChatUserInfo (
				null);

		// update queue item stuff

		Responder responder =
			updateQueueItem (
				chatUser,
				myUser);

		transaction.commit ();

		// add a notice

		requestContext.addNotice (
			"Chat user info approved");

		// and we're done

		return responder;

	}

	private
	Responder goApproveImage (
			PendingMode mode) {

		Responder responder;

		ChatUserImageType imageType =
			chatUserLogic.imageTypeForMode (mode);

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		// get database objects

		ChatUserRec chatUser =
			chatUserHelper.find (
				requestContext.stuffInt ("chatUserId"));

		UserRec myUser =
			userHelper.find (
				requestContext.userId ());

		// confirm there is something to approve

		ChatUserImageRec chatUserImage =
			chatUserLogic.chatUserPendingImage (
				chatUser,
				imageType);

		if (chatUserImage == null) {

			requestContext.addError (
				stringFormat (
					"No %s to approve",
					mode));

			return nextResponder (
				chatUser);

		}

		// update the chat user and stuff

		@SuppressWarnings ("unchecked")
		List<ChatUserImageRec> list =
			(List<ChatUserImageRec>)
			BeanLogic.getProperty (
				chatUser,
				mode.listProperty ());

		if (
			notEqual (
				chatUserImage.getStatus (),
				ChatUserInfoStatus.moderatorPending)
		) {

			throw new RuntimeException ();

		}

		chatUserImage

			.setModerator (
				myUser)

			.setStatus (
				ChatUserInfoStatus.moderatorApproved)

			.setModerationTime (
				instantToDate (
					transaction.now ()))

			.setIndex (
				list.size ())

			.setClassification (
				requestContext.parameter ("classification"));

		// update main image if not in append mode

		if (! chatUserImage.getAppend ()) {

			ChatUserImageRec oldChatUserImage =
				chatUser.getMainChatUserImageByType (
					imageType);

			if (oldChatUserImage != null) {

				int index =
					oldChatUserImage.getIndex ();

				oldChatUserImage.setIndex (null);

				transaction.flush ();

				chatUserImage.setIndex (index);

			}

			chatUser.setMainChatUserImageByType (
				imageType,
				chatUserImage);

		}

		responder =
			updateQueueItem (
				chatUser,
				myUser);

		transaction.commit ();

		// add a notice

		requestContext.addNotice (
			"Chat user image approved");

		// and we're done

		return responder;

	}

	private
	Responder goRejectName () {

		// get params

		String messageParam =
			requestContext.parameter ("message").trim ();

		if (Gsm.length (messageParam) == 0) {

			requestContext.addError (
				"Please enter a message to send");

			return null;

		}

		if (Gsm.length (messageParam) > 160) {

			requestContext.addError (
				"Message is too long");

			return null;

		}

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		// get database objects

		ChatUserRec chatUser =
			chatUserHelper.find (
				requestContext.stuffInt ("chatUserId"));

		UserRec myUser =
			userHelper.find (
				requestContext.userId ());

		if (chatUser.getNewChatUserName() == null) {

			requestContext.addError (
				"No name to approve");

			return nextResponder (
				chatUser);

		}

		ChatUserNameRec chatUserName =
			chatUser.getNewChatUserName ();

		if (chatUserName.getStatus () != ChatUserInfoStatus.moderatorPending)
			throw new RuntimeException ();

		chatUserName

			.setStatus (
				ChatUserInfoStatus.moderatorRejected)

			.setModerationTime (
				new Date ())

			.setModerator (
				myUser);

		chatUser

			.setNewChatUserName (
				null);

		// send rejection

		sendRejection (
			myUser,
			chatUser,
			chatUserName.getThreadId (),
			messageParam);

		Responder responder =
			updateQueueItem (
				chatUser,
				myUser);

		transaction.commit ();

		requestContext.addNotice (
			"Rejection sent");

		return responder;

	}

	private
	Responder goRejectInfo () {

		// get params

		String messageParam =
			requestContext.parameter ("message").trim ();

		if (Gsm.length (messageParam) == 0) {

			requestContext.addError (
				"Please enter a message to send");

			return null;

		}

		if (Gsm.length (messageParam) > 160) {

			requestContext.addError (
				"Message is too long");

			return null;

		}

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		// get database objects

		ChatUserRec chatUser =
			chatUserHelper.find (
				requestContext.stuffInt ("chatUserId"));

		UserRec myUser =
			userHelper.find (
				requestContext.userId ());

		// confirm there is something to approve

		if (chatUser.getNewChatUserInfo () == null) {

			requestContext.addError (
				"No info to approve");

			return nextResponder (
				chatUser);

		}

		// update chat user info

		ChatUserInfoRec chatUserInfo =
			chatUser.getNewChatUserInfo ();

		if (
			notEqual (
				chatUserInfo.getStatus (),
				ChatUserInfoStatus.moderatorPending)
		) {

			throw new RuntimeException ();

		}

		chatUserInfo

			.setStatus (
				ChatUserInfoStatus.moderatorRejected)

			.setModerationTime (
				new Date ())

			.setModerator (
				myUser);

		// update chat user

		chatUser

			.setNewChatUserInfo (
				null);

		// send rejection

		sendRejection (
			myUser,
			chatUser,
			chatUserInfo.getThreadId (),
			messageParam);

		Responder responder =
			updateQueueItem (
				chatUser,
				myUser);

		transaction.commit ();

		requestContext.addNotice (
			"Rejection sent");

		return responder;

	}

	private
	void sendRejection (
			UserRec myUser,
			ChatUserRec chatUser,
			Integer threadId,
			String messageParam) {

		ChatRec chat =
			chatUser.getChat ();

		MessageRec message = null;

		ChatMessageRec chatMessage = null;

		if (

			in (chatUser.getDeliveryMethod (),
				ChatMessageMethod.iphone,
				ChatMessageMethod.web)

			&& chat.getSystemChatUser () != null

		) {

			TextRec messageText =
				textHelper.findOrCreate (messageParam);

			chatMessage =
				objectManager.insert (
					new ChatMessageRec ()

				.setFromUser (
					chat.getSystemChatUser ())

				.setToUser (
					chatUser)

				.setChat (
					chat)

				.setTimestamp (
					new Date ())

				.setSender (
					myUser)

				.setChat (
					chat)

				.setOriginalText (
					messageText)

				.setEditedText (
					messageText)

				.setStatus (
					ChatMessageStatus.sent)

			);

			chatMessageLogic.chatMessageDeliverToUser (
				chatMessage);

		} else {

			TextRec messageText =
				textHelper.findOrCreate (messageParam);

			message =
				chatSendLogic.sendMessageMagic (
					chatUser,
					Optional.of (threadId),
					messageText,
					commandHelper.findByCode (chat, "join_info"),
					serviceHelper.findByCode (chat, "system"),
					0);

		}

		chatHelpLogLogic.createChatHelpLogOut (
			chatUser,
			null,
			myUser,
			message,
			chatMessage,
			messageParam,
			commandHelper.findByCode (chat, "join_info"));

	}

	private
	Responder goRejectImage (
			PendingMode mode) {

		// check params

		String messageParam =
			requestContext.parameter ("message").trim ();

		if (Gsm.length (messageParam) == 0) {
			requestContext.addError ("Please enter a message to send");
			return null;
		}

		if (Gsm.length (messageParam) > 160) {
			requestContext.addError ("Message is too long");
			return null;
		}

		// start transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		ChatUserRec chatUser =
			chatUserHelper.find (
				requestContext.stuffInt ("chatUserId"));

		ChatRec chat =
			chatUser.getChat ();

		UserRec myUser =
			userHelper.find (
				requestContext.userId ());

		ChatUserImageRec chatUserImage =
			chatUserLogic.chatUserPendingImage (
				chatUser,
				ChatUserImageType.valueOf (mode.toString ()));

		// checks involving database

		if (chatUserImage == null) {

			requestContext.addError (
				"No photo to approve");

			return nextResponder (
				chatUser);

		}

		if (chatUserImage.getStatus ()
				!= ChatUserInfoStatus.moderatorPending)
			throw new RuntimeException ();

		// update image

		chatUserImage

			.setStatus (
				ChatUserInfoStatus.moderatorRejected)

			.setModerationTime (
				new Date ())

			.setModerator (
				myUser);

		// send message

		MessageRec message = null;
		ChatMessageRec chatMessage = null;

		if (

			in (chatUser.getDeliveryMethod (),
				ChatMessageMethod.iphone,
				ChatMessageMethod.web)

			&& chat.getSystemChatUser () != null

		) {

			// iphone/web

			TextRec messageText =
				textHelper.findOrCreate (messageParam);

			chatMessage =
				objectManager.insert (
					new ChatMessageRec ()

				.setFromUser (
					chat.getSystemChatUser ())

				.setToUser (
					chatUser)

				.setChat (
					chat)

				.setTimestamp (
					new Date ())

				.setSender (
					myUser)

				.setChat (
					chat)

				.setOriginalText (
					messageText)

				.setEditedText (
					messageText)

				.setStatus (
					ChatMessageStatus.sent)

			);

			chatMessageLogic.chatMessageDeliverToUser (
				chatMessage);

		} else {

			// sms

			message =
				chatSendLogic.sendMessageMmsFree (
					chatUser,
					Optional.<Integer>absent (),
					messageParam,
					commandHelper.findByCode (
						chat,
						mode.commandCode ()),
					serviceHelper.findByCode (
						chat,
						"system"));

		}

		// log message sent

		chatHelpLogLogic.createChatHelpLogOut (
			chatUser,
			null,
			myUser,
			message,
			chatMessage,
			messageParam,
			commandHelper.findByCode (
				chat,
				mode.commandCode ()));

		// clear queue item

		Responder responder =
			updateQueueItem (
				chatUser,
				myUser);

		// wrap up

		transaction.commit ();

		requestContext.addNotice (
			"Rejection sent");

		return responder;

	}

	/**
	 * Expires the existing queue item associated with the chat user and
	 * creates a new one if there is still anything to approve.
	 */
	private
	Responder updateQueueItem (
			ChatUserRec chatUser,
			UserRec myUser) {

		if (moreToApprove (chatUser)) {

			return responder (
				"chatUserPendingFormResponder");

		}

		queueLogic.processQueueItem (
			chatUser.getQueueItem (),
			myUser);

		chatUser

			.setQueueItem (
				null);

		return responder (
			"queueHomeResponder");

	}

	private
	boolean moreToApprove (
			ChatUserRec chatUser) {

		if (chatUser.getNewChatUserName () != null)
			return true;

		if (chatUser.getNewChatUserInfo () != null)
			return true;

		if (
			chatUserLogic.chatUserPendingImage (
				chatUser,
				ChatUserImageType.image) != null
		) {
			return true;
		}

		if (
			chatUserLogic.chatUserPendingImage (
				chatUser,
				ChatUserImageType.video) != null
		) {
			return true;
		}

		if (
			chatUserLogic.chatUserPendingImage (
				chatUser,
				ChatUserImageType.audio) != null
		) {
			return true;
		}

		return false;

	}

	private
	Responder nextResponder (
			ChatUserRec chatUser) {

		return responder (
			moreToApprove (chatUser)
				? "chatUserPendingFormResponder"
				: "queueHomeResponder");

	}

}
