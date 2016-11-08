package wbs.apn.chat.user.pending.console;

import static wbs.sms.gsm.GsmUtils.gsmStringLength;
import static wbs.utils.etc.EnumUtils.enumInSafe;
import static wbs.utils.etc.EnumUtils.enumNameSpaces;
import static wbs.utils.etc.EnumUtils.enumNotEqualSafe;
import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.Misc.stringTrim;
import static wbs.utils.etc.NumberUtils.equalToZero;
import static wbs.utils.etc.NumberUtils.fromJavaInteger;
import static wbs.utils.etc.NumberUtils.moreThan;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.stringEqualSafe;

import java.util.List;

import com.google.common.base.Optional;

import lombok.Cleanup;
import lombok.NonNull;

import wbs.apn.chat.contact.console.ChatMessageConsoleHelper;
import wbs.apn.chat.contact.logic.ChatMessageLogic;
import wbs.apn.chat.contact.logic.ChatSendLogic;
import wbs.apn.chat.contact.model.ChatMessageMethod;
import wbs.apn.chat.contact.model.ChatMessageRec;
import wbs.apn.chat.contact.model.ChatMessageStatus;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.help.logic.ChatHelpLogLogic;
import wbs.apn.chat.help.model.ChatHelpLogRec;
import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.logic.ChatUserLogic.PendingMode;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.image.model.ChatUserImageRec;
import wbs.apn.chat.user.image.model.ChatUserImageType;
import wbs.apn.chat.user.info.model.ChatUserInfoRec;
import wbs.apn.chat.user.info.model.ChatUserInfoStatus;
import wbs.apn.chat.user.info.model.ChatUserNameRec;
import wbs.console.action.ConsoleAction;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.TaskLogger;
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
import wbs.utils.etc.PropertyUtils;

@PrototypeComponent ("chatUserPendingFormAction")
public
class ChatUserPendingFormAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	ChatHelpLogLogic chatHelpLogLogic;

	@SingletonDependency
	ChatSendLogic chatSendLogic;

	@SingletonDependency
	ChatMessageConsoleHelper chatMessageHelper;

	@SingletonDependency
	ChatMessageLogic chatMessageLogic;

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@SingletonDependency
	ChatUserLogic chatUserLogic;

	@SingletonDependency
	CommandObjectHelper commandHelper;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	Database database;

	@SingletonDependency
	QueueLogic queueLogic;

	@SingletonDependency
	ServiceObjectHelper serviceHelper;

	@SingletonDependency
	TextObjectHelper textHelper;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
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
				requestContext.stuffInteger (
					"chatUserId"));

		return nextResponder (
			chatUser);

	}

	// implementation

	@Override
	protected
	Responder goReal (
			@NonNull TaskLogger taskLogger) {

		// delegate appropriately

		if (
			optionalIsPresent (
				requestContext.parameter (
					"chatUserDismiss"))
		) {
			return goDismiss ();
		}

		if (
			optionalIsPresent (
				requestContext.parameter (
					"chatUserNameApprove"))
		) {
			return goApproveName ();
		}

		if (
			optionalIsPresent (
				requestContext.parameter (
					"chatUserInfoApprove"))
		) {
			return goApproveInfo ();
		}

		if (
			optionalIsPresent (
				requestContext.parameter (
					"chatUserImageApprove"))
		) {

			return goApproveImage (
				PendingMode.image);

		}

		if (
			optionalIsPresent (
				requestContext.parameter (
					"chatUserVideoApprove"))
		) {

			return goApproveImage (
				PendingMode.video);

		}

		if (
			optionalIsPresent (
				requestContext.parameter (
					"chatUserAudioApprove"))
		) {

			return goApproveImage (
				PendingMode.audio);

		}

		if (
			optionalIsPresent (
				requestContext.parameter (
					"chatUserNameReject"))
		) {
			return goRejectName ();
		}

		if (
			optionalIsPresent (
				requestContext.parameter (
					"chatUserInfoReject"))
		) {
			return goRejectInfo ();
		}

		if (
			optionalIsPresent (
				requestContext.parameter (
					"chatUserImageReject"))
		) {

			return goRejectImage (
				PendingMode.image);

		}

		if (
			optionalIsPresent (
				requestContext.parameter (
					"chatUserVideoReject"))
		) {

			return goRejectImage (
				PendingMode.video);

		}

		if (
			optionalIsPresent (
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
				requestContext.stuffInteger (
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
				requestContext.stuffInteger (
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
			enumNotEqualSafe (
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
				ifThenElse (
					stringEqualSafe (
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
				requestContext.stuffInteger (
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
			enumNotEqualSafe (
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
				ifThenElse (
					stringEqualSafe (
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
				requestContext.stuffInteger (
					"chatUserId"));

		// confirm there is something to approve

		ChatUserImageRec chatUserImage =
			chatUserLogic.chatUserPendingImage (
				chatUser,
				imageType);

		if (chatUserImage == null) {

			requestContext.addErrorFormat (
				"No %s to approve",
				enumNameSpaces (
					mode));

			return nextResponder (
				chatUser);

		}

		// update the chat user and stuff

		List <ChatUserImageRec> list =
			genericCastUnchecked (
				PropertyUtils.getProperty (
					chatUser,
					mode.listProperty ()));

		if (
			enumNotEqualSafe (
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
				fromJavaInteger (
					list.size ()))

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
			stringTrim (
				requestContext.parameterRequired (
					"message"));

		if (GsmUtils.gsmStringLength (messageParam) == 0) {

			requestContext.addError (
				"Please enter a message to send");

			return null;

		}

		if (GsmUtils.gsmStringLength (messageParam) > 160) {

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
				requestContext.stuffInteger (
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
			stringTrim (
				requestContext.parameterRequired (
					"message"));

		if (GsmUtils.gsmStringLength (messageParam) == 0) {

			requestContext.addError (
				"Please enter a message to send");

			return null;

		}

		if (GsmUtils.gsmStringLength (messageParam) > 160) {

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
				requestContext.stuffInteger (
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
			enumNotEqualSafe (
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

		Optional <ChatMessageRec> chatMessage;

		if (

			enumInSafe (
				chatUser.getDeliveryMethod (),
				ChatMessageMethod.iphone,
				ChatMessageMethod.web)

			&& isNotNull (
				chat.getSystemChatUser ())

		) {

			TextRec messageText =
				textHelper.findOrCreate (
					messageParam);

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
			stringTrim (
				requestContext.parameterRequired (
					"message"));

		Long messageParamGsmLength =
			gsmStringLength (
				messageParam);

		if (
			equalToZero (
				messageParamGsmLength)
		) {

			requestContext.addError (
				"Please enter a message to send");

			return null;

		}

		if (
			moreThan (
				messageParamGsmLength,
				160l)
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
				requestContext.stuffInteger (
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

			enumInSafe (
				chatUser.getDeliveryMethod (),
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
