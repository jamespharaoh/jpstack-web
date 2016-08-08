package wbs.clients.apn.chat.user.pending.console;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.ifElse;
import static wbs.framework.utils.etc.Misc.in;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.Misc.isZero;
import static wbs.framework.utils.etc.Misc.moreThan;
import static wbs.framework.utils.etc.Misc.notEqual;
import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.Misc.trim;

import java.util.List;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.NonNull;

import com.google.common.base.Optional;

import wbs.clients.apn.chat.contact.console.ChatMessageConsoleHelper;
import wbs.clients.apn.chat.contact.logic.ChatMessageLogic;
import wbs.clients.apn.chat.contact.logic.ChatSendLogic;
import wbs.clients.apn.chat.contact.model.ChatMessageMethod;
import wbs.clients.apn.chat.contact.model.ChatMessageRec;
import wbs.clients.apn.chat.contact.model.ChatMessageStatus;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.help.logic.ChatHelpLogLogic;
import wbs.clients.apn.chat.help.model.ChatHelpLogRec;
import wbs.clients.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.clients.apn.chat.user.core.logic.ChatUserLogic;
import wbs.clients.apn.chat.user.core.logic.ChatUserLogic.PendingMode;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.image.model.ChatUserImageRec;
import wbs.clients.apn.chat.user.image.model.ChatUserImageType;
import wbs.clients.apn.chat.user.info.model.ChatUserInfoRec;
import wbs.clients.apn.chat.user.info.model.ChatUserInfoStatus;
import wbs.clients.apn.chat.user.info.model.ChatUserNameRec;
import wbs.console.action.ConsoleAction;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.utils.etc.BeanLogic;
import static wbs.framework.utils.etc.OptionalUtils.isPresent;
import wbs.framework.web.Responder;
import wbs.platform.queue.logic.QueueLogic;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextRec;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.gsm.GsmUtils;
import wbs.sms.message.core.model.MessageRec;

@PrototypeComponent ("chatUserPendingFormAction")
public
class ChatUserPendingFormAction
	extends ConsoleAction {

	// dependencies

	@Inject
	ChatHelpLogLogic chatHelpLogLogic;

	@Inject
	ChatSendLogic chatSendLogic;

	@Inject
	ChatMessageConsoleHelper chatMessageHelper;

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
	UserConsoleLogic userConsoleLogic;

	@Inject
	UserObjectHelper userHelper;

	// details

	@Override
	protected
	Responder backupResponder () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ChatUserPendingFormAction.backupResponder ()",
				this);

		ChatUserRec chatUser =
			chatUserHelper.findRequired (
				requestContext.stuffInt (
					"chatUserId"));

		return nextResponder (
			chatUser);

	}

	// implementation

	@Override
	protected
	Responder goReal () {

		// delegate appropriately

		if (
			isPresent (
				requestContext.parameter (
					"chatUserDismiss"))
		) {
			return goDismiss ();
		}

		if (
			isPresent (
				requestContext.parameter (
					"chatUserNameApprove"))
		) {
			return goApproveName ();
		}

		if (
			isPresent (
				requestContext.parameter (
					"chatUserInfoApprove"))
		) {
			return goApproveInfo ();
		}

		if (
			isPresent (
				requestContext.parameter (
					"chatUserImageApprove"))
		) {

			return goApproveImage (
				PendingMode.image);

		}

		if (
			isPresent (
				requestContext.parameter (
					"chatUserVideoApprove"))
		) {

			return goApproveImage (
				PendingMode.video);

		}

		if (
			isPresent (
				requestContext.parameter (
					"chatUserAudioApprove"))
		) {

			return goApproveImage (
				PendingMode.audio);

		}

		if (
			isPresent (
				requestContext.parameter (
					"chatUserNameReject"))
		) {
			return goRejectName ();
		}

		if (
			isPresent (
				requestContext.parameter (
					"chatUserInfoReject"))
		) {
			return goRejectInfo ();
		}

		if (
			isPresent (
				requestContext.parameter (
					"chatUserImageReject"))
		) {

			return goRejectImage (
				PendingMode.image);

		}

		if (
			isPresent (
				requestContext.parameter (
					"chatUserVideoReject"))
		) {

			return goRejectImage (
				PendingMode.video);

		}

		if (
			isPresent (
				requestContext.parameter (
					"chatUserAudioReject"))
		) {

			return goRejectImage (
				PendingMode.audio);

		}

		throw new RuntimeException (
			"Invalid parameters");

	}

	private
	Responder goDismiss () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ChatUserPendingFormAction.goDismiss ()",
				this);

		ChatUserRec chatUser =
			chatUserHelper.findRequired (
				requestContext.stuffInt (
					"chatUserId"));

		Responder responder =
			updateQueueItem (
				chatUser,
				userConsoleLogic.userRequired ());

		transaction.commit ();

		return responder;

	}

	private
	Responder goApproveName () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ChatUserPendingFormAction.goApproveName ()",
				this);

		// get database objects

		ChatUserRec chatUser =
			chatUserHelper.findRequired (
				requestContext.stuffInt (
					"chatUserId"));

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

		String editedName =
			requestContext.parameterRequired (
				"name");

		chatUserName

			.setModerator (
				userConsoleLogic.userRequired ())

			.setStatus (
				ifElse (
					equal (
						editedName,
						chatUserName.getOriginalName ()),
					() -> ChatUserInfoStatus.moderatorApproved,
					() -> ChatUserInfoStatus.moderatorEdited))

			.setModerationTime (
				transaction.now ())

			.setEditedName (
				editedName);

		chatUser

			.setName (
				editedName)

			.setNewChatUserName (
				null);

		// remove the queue item and create any new one

		Responder responder =
			updateQueueItem (
				chatUser,
				userConsoleLogic.userRequired ());

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
			database.beginReadWrite (
				"ChatUserPendingFormAction.goApproveInfo ()",
				this);

		// get database objects

		ChatUserRec chatUser =
			chatUserHelper.findRequired (
				requestContext.stuffInt (
					"chatUserId"));

		// confirm there is something to approve

		if (
			isNull (
				chatUser.getNewChatUserInfo ())
		) {

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

		String editedInfo =
			requestContext.parameterRequired (
				"info");

		TextRec editedText =
			textHelper.findOrCreate (
				editedInfo);

		chatUserInfo

			.setModerator (
				userConsoleLogic.userRequired ())

			.setStatus (
				ifElse (
					equal (
						editedInfo,
						chatUserInfo.getOriginalText ().getText ()),
					() -> ChatUserInfoStatus.moderatorApproved,
					() -> ChatUserInfoStatus.moderatorEdited))

			.setModerationTime (
				transaction.now ())

			.setEditedText (
				editedText);

		chatUser

			.setInfoText (
				editedText)

			.setNewChatUserInfo (
				null);

		// update queue item stuff

		Responder responder =
			updateQueueItem (
				chatUser,
				userConsoleLogic.userRequired ());

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
			database.beginReadWrite (
				"ChatUserPendingFormAction.goApproveImage",
				this);

		// get database objects

		ChatUserRec chatUser =
			chatUserHelper.findRequired (
				requestContext.stuffInt (
					"chatUserId"));

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
				userConsoleLogic.userRequired ())

			.setStatus (
				ChatUserInfoStatus.moderatorApproved)

			.setModerationTime (
				transaction.now ())

			.setIndex (
				(long)
				list.size ())

			.setClassification (
				requestContext.parameterRequired (
					"classification"));

		// update main image if not in append mode

		if (! chatUserImage.getAppend ()) {

			ChatUserImageRec oldChatUserImage =
				chatUserLogic.getMainChatUserImageByType (
					chatUser,
					imageType);

			if (oldChatUserImage != null) {

				long index =
					oldChatUserImage.getIndex ();

				oldChatUserImage.setIndex (null);

				transaction.flush ();

				chatUserImage

					.setIndex (
						index);

			}

			chatUserLogic.setMainChatUserImageByType (
				chatUser,
				imageType,
				Optional.of (
					chatUserImage));

		}

		responder =
			updateQueueItem (
				chatUser,
				userConsoleLogic.userRequired ());

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
			trim (
				requestContext.parameterRequired (
					"message"));

		if (GsmUtils.length (messageParam) == 0) {

			requestContext.addError (
				"Please enter a message to send");

			return null;

		}

		if (GsmUtils.length (messageParam) > 160) {

			requestContext.addError (
				"Message is too long");

			return null;

		}

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ChatUserPendingFormAction.goRejectName ()",
				this);

		// get database objects

		ChatUserRec chatUser =
			chatUserHelper.findRequired (
				requestContext.stuffInt (
					"chatUserId"));

		if (
			isNull (
				chatUser.getNewChatUserName ())
		) {

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
				transaction.now ())

			.setModerator (
				userConsoleLogic.userRequired ());

		chatUser

			.setNewChatUserName (
				null);

		// send rejection

		sendRejection (
			userConsoleLogic.userRequired (),
			chatUser,
			Optional.fromNullable (
				chatUserName.getThreadId ()),
			messageParam);

		Responder responder =
			updateQueueItem (
				chatUser,
				userConsoleLogic.userRequired ());

		transaction.commit ();

		requestContext.addNotice (
			"Rejection sent");

		return responder;

	}

	private
	Responder goRejectInfo () {

		// get params

		String messageParam =
			trim (
				requestContext.parameterRequired (
					"message"));

		if (GsmUtils.length (messageParam) == 0) {

			requestContext.addError (
				"Please enter a message to send");

			return null;

		}

		if (GsmUtils.length (messageParam) > 160) {

			requestContext.addError (
				"Message is too long");

			return null;

		}

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ChatUserPendingFormAction.goRejectInfo ()",
				this);

		// get database objects

		ChatUserRec chatUser =
			chatUserHelper.findRequired (
				requestContext.stuffInt (
					"chatUserId"));

		// confirm there is something to approve

		if (
			isNull (
				chatUser.getNewChatUserInfo ())
		) {

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
				transaction.now ())

			.setModerator (
				userConsoleLogic.userRequired ());

		// update chat user

		chatUser

			.setNewChatUserInfo (
				null);

		// send rejection

		sendRejection (
			userConsoleLogic.userRequired (),
			chatUser,
			Optional.fromNullable (
				chatUserInfo.getThreadId ()),
			messageParam);

		Responder responder =
			updateQueueItem (
				chatUser,
				userConsoleLogic.userRequired ());

		transaction.commit ();

		requestContext.addNotice (
			"Rejection sent");

		return responder;

	}

	private
	void sendRejection (
			@NonNull UserRec myUser,
			@NonNull ChatUserRec chatUser,
			@NonNull Optional<Long> threadId,
			@NonNull String messageParam) {

		Transaction transaction =
			database.currentTransaction ();

		ChatRec chat =
			chatUser.getChat ();

		MessageRec message = null;

		Optional<ChatMessageRec> chatMessage;

		if (

			in (chatUser.getDeliveryMethod (),
				ChatMessageMethod.iphone,
				ChatMessageMethod.web)

			&& chat.getSystemChatUser () != null

		) {

			TextRec messageText =
				textHelper.findOrCreate (messageParam);

			chatMessage =
				Optional.of (
					chatMessageHelper.insert (
						chatMessageHelper.createInstance ()

				.setFromUser (
					chat.getSystemChatUser ())

				.setToUser (
					chatUser)

				.setTimestamp (
					transaction.now ())

				.setChat (
					chat)

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

			));

			chatMessageLogic.chatMessageDeliverToUser (
				chatMessage.get ());

		} else {

			TextRec messageText =
				textHelper.findOrCreate (
					messageParam);

			message =
				chatSendLogic.sendMessageMagic (
					chatUser,
					threadId,
					messageText,
					commandHelper.findByCodeRequired (
						chat,
						"join_info"),
					serviceHelper.findByCodeRequired (
						chat,
						"system"),
					0l);

			chatMessage =
				Optional.<ChatMessageRec>absent ();

		}

		chatHelpLogLogic.createChatHelpLogOut (
			chatUser,
			Optional.<ChatHelpLogRec>absent (),
			Optional.of (
				myUser),
			message,
			chatMessage,
			messageParam,
			Optional.of (
				commandHelper.findByCodeRequired (
					chat,
					"join_info")));

	}

	private
	Responder goRejectImage (
			PendingMode mode) {

		// check params

		String messageParam =
			trim (
				requestContext.parameterRequired (
					"message"));

		if (
			isZero (
				GsmUtils.length (
					messageParam))
		) {

			requestContext.addError (
				"Please enter a message to send");

			return null;

		}

		if (
			moreThan (
				GsmUtils.length (
					messageParam),
				160)
		) {

			requestContext.addError (
				"Message is too long");

			return null;

		}

		// start transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ChatUserPendingFormAction.goRejectImage ()",
				this);

		ChatUserRec chatUser =
			chatUserHelper.findRequired (
				requestContext.stuffInt (
					"chatUserId"));

		ChatRec chat =
			chatUser.getChat ();

		ChatUserImageRec chatUserImage =
			chatUserLogic.chatUserPendingImage (
				chatUser,
				ChatUserImageType.valueOf (
					mode.toString ()));

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
				transaction.now ())

			.setModerator (
				userConsoleLogic.userRequired ());

		// send message

		MessageRec message = null;
		ChatMessageRec chatMessage = null;

		if (

			in (chatUser.getDeliveryMethod (),
				ChatMessageMethod.iphone,
				ChatMessageMethod.web)

			&& isNotNull (
				chat.getSystemChatUser ())

		) {

			// iphone/web

			TextRec messageText =
				textHelper.findOrCreate (messageParam);

			chatMessage =
				chatMessageHelper.insert (
					chatMessageHelper.createInstance ()

				.setFromUser (
					chat.getSystemChatUser ())

				.setToUser (
					chatUser)

				.setTimestamp (
					transaction.now ())

				.setChat (
					chat)

				.setSender (
					userConsoleLogic.userRequired ())

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
					Optional.<Long>absent (),
					messageParam,
					commandHelper.findByCodeRequired (
						chat,
						mode.commandCode ()),
					serviceHelper.findByCodeRequired (
						chat,
						"system"));

		}

		// log message sent

		chatHelpLogLogic.createChatHelpLogOut (
			chatUser,
			Optional.<ChatHelpLogRec>absent (),
			Optional.of (
				userConsoleLogic.userRequired ()),
			message,
			Optional.fromNullable (
				chatMessage),
			messageParam,
			Optional.of (
				commandHelper.findByCodeRequired (
					chat,
					mode.commandCode ())));

		// clear queue item

		Responder responder =
			updateQueueItem (
				chatUser,
				userConsoleLogic.userRequired ());

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
