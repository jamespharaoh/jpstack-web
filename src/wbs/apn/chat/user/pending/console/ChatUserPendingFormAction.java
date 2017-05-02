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
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.stringEqualSafe;

import java.util.List;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.action.ConsoleAction;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

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

import wbs.apn.chat.contact.console.ChatMessageConsoleHelper;
import wbs.apn.chat.contact.logic.ChatMessageLogic;
import wbs.apn.chat.contact.logic.ChatSendLogic;
import wbs.apn.chat.contact.model.ChatMessageMethod;
import wbs.apn.chat.contact.model.ChatMessageRec;
import wbs.apn.chat.contact.model.ChatMessageStatus;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.help.logic.ChatHelpLogLogic;
import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.logic.ChatUserLogic.PendingMode;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.image.model.ChatUserImageRec;
import wbs.apn.chat.user.image.model.ChatUserImageType;
import wbs.apn.chat.user.info.model.ChatUserInfoRec;
import wbs.apn.chat.user.info.model.ChatUserInfoStatus;
import wbs.apn.chat.user.info.model.ChatUserNameRec;
import wbs.web.responder.Responder;

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
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	QueueLogic queueLogic;

	@SingletonDependency
	ConsoleRequestContext requestContext;

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
	Responder backupResponder (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadOnly (
					logContext,
					"backupResponder");

		) {

			ChatUserRec chatUser =
				chatUserHelper.findFromContextRequired (
					transaction);

			return nextResponder (
				transaction,
				chatUser);

		}

	}

	// implementation

	@Override
	protected
	Responder goReal (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"goReal");

		) {

			// delegate appropriately

			if (
				optionalIsPresent (
					requestContext.parameter (
						"chatUserDismiss"))
			) {

				return goDismiss (
					taskLogger);

			}

			if (
				optionalIsPresent (
					requestContext.parameter (
						"chatUserNameApprove"))
			) {

				return goApproveName (
					taskLogger);

			}

			if (
				optionalIsPresent (
					requestContext.parameter (
						"chatUserInfoApprove"))
			) {

				return goApproveInfo (
					taskLogger);

			}

			if (
				optionalIsPresent (
					requestContext.parameter (
						"chatUserImageApprove"))
			) {

				return goApproveImage (
					taskLogger,
					PendingMode.image);

			}

			if (
				optionalIsPresent (
					requestContext.parameter (
						"chatUserVideoApprove"))
			) {

				return goApproveImage (
					taskLogger,
					PendingMode.video);

			}

			if (
				optionalIsPresent (
					requestContext.parameter (
						"chatUserAudioApprove"))
			) {

				return goApproveImage (
					taskLogger,
					PendingMode.audio);

			}

			if (
				optionalIsPresent (
					requestContext.parameter (
						"chatUserNameReject"))
			) {

				return goRejectName (
					taskLogger);

			}

			if (
				optionalIsPresent (
					requestContext.parameter (
						"chatUserInfoReject"))
			) {

				return goRejectInfo (
					taskLogger);

			}

			if (
				optionalIsPresent (
					requestContext.parameter (
						"chatUserImageReject"))
			) {

				return goRejectImage (
					taskLogger,
					PendingMode.image);

			}

			if (
				optionalIsPresent (
					requestContext.parameter (
						"chatUserVideoReject"))
			) {

				return goRejectImage (
					taskLogger,
					PendingMode.video);

			}

			if (
				optionalIsPresent (
					requestContext.parameter (
						"chatUserAudioReject"))
			) {

				return goRejectImage (
					taskLogger,
					PendingMode.audio);

			}

			throw new RuntimeException (
				"Invalid parameters");

		}

	}

	private
	Responder goDismiss (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"goDismiss");

		) {

			ChatUserRec chatUser =
				chatUserHelper.findFromContextRequired (
					transaction);

			Responder responder =
				updateQueueItem (
					transaction,
					chatUser,
					userConsoleLogic.userRequired (
						transaction));

			transaction.commit ();

			return responder;

		}

	}

	private
	Responder goApproveName (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"goApproveName");

		) {

			// get database objects

			ChatUserRec chatUser =
				chatUserHelper.findFromContextRequired (
					transaction);

			// confirm there is something to approve

			if (chatUser.getNewChatUserName () == null) {

				requestContext.addError (
					"No name to approve");

				return nextResponder (
					transaction,
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
					userConsoleLogic.userRequired (
						transaction))

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
					transaction,
					chatUser,
					userConsoleLogic.userRequired (
						transaction));

			transaction.commit ();

			// add a notice

			requestContext.addNotice (
				"Chat user name approved");

			// and return

			return responder;

		}

	}

	private
	Responder goApproveInfo (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"goApproveInfo");

		) {

			// get database objects

			ChatUserRec chatUser =
				chatUserHelper.findFromContextRequired (
					transaction);

			// confirm there is something to approve

			if (
				isNull (
					chatUser.getNewChatUserInfo ())
			) {

				requestContext.addError (
					"No info to approve");

				return nextResponder (
					transaction,
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
					transaction,
					editedInfo);

			chatUserInfo

				.setModerator (
					userConsoleLogic.userRequired (
						transaction))

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
					transaction,
					chatUser,
					userConsoleLogic.userRequired (
						transaction));

			transaction.commit ();

			// add a notice

			requestContext.addNotice (
				"Chat user info approved");

			// and we're done

			return responder;

		}

	}

	private
	Responder goApproveImage (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull PendingMode mode) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"goApproveImage");

		) {

			Responder responder;

			ChatUserImageType imageType =
				chatUserLogic.imageTypeForMode (
					mode);

			// get database objects

			ChatUserRec chatUser =
				chatUserHelper.findFromContextRequired (
					transaction);

			// confirm there is something to approve

			ChatUserImageRec chatUserImage =
				chatUserLogic.chatUserPendingImage (
					transaction,
					chatUser,
					imageType);

			if (chatUserImage == null) {

				requestContext.addErrorFormat (
					"No %s to approve",
					enumNameSpaces (
						mode));

				return nextResponder (
					transaction,
					chatUser);

			}

			// update the chat user and stuff

			List <ChatUserImageRec> list =
				genericCastUnchecked (
					PropertyUtils.propertyGetAuto (
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
					userConsoleLogic.userRequired (
						transaction))

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
					transaction,
					chatUser,
					userConsoleLogic.userRequired (
						transaction));

			transaction.commit ();

			// add a notice

			requestContext.addNotice (
				"Chat user image approved");

			// and we're done

			return responder;

		}

	}

	private
	Responder goRejectName (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"goRejectName");

		) {

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

			try (

				OwnedTransaction transaction =
					database.beginReadWrite (
						logContext,
						parentTaskLogger,
						"goRejectName");

			) {

				// get database objects

				ChatUserRec chatUser =
					chatUserHelper.findFromContextRequired (
						transaction);

				if (
					isNull (
						chatUser.getNewChatUserName ())
				) {

					requestContext.addError (
						"No name to approve");

					return nextResponder (
						transaction,
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
						userConsoleLogic.userRequired (
							transaction));

				chatUser

					.setNewChatUserName (
						null);

				// send rejection

				sendRejection (
					transaction,
					userConsoleLogic.userRequired (
						transaction),
					chatUser,
					optionalFromNullable (
						chatUserName.getThreadId ()),
					messageParam);

				Responder responder =
					updateQueueItem (
						transaction,
						chatUser,
						userConsoleLogic.userRequired (
							transaction));

				transaction.commit ();

				requestContext.addNotice (
					"Rejection sent");

				return responder;

			}

		}

	}

	private
	Responder goRejectInfo (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"goRejectInfo");

		) {

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

			try (

				OwnedTransaction transaction =
					database.beginReadWrite (
						logContext,
						parentTaskLogger,
						"goRejectInfo");

			) {

				// get database objects

				ChatUserRec chatUser =
					chatUserHelper.findFromContextRequired (
						transaction);

				// confirm there is something to approve

				if (
					isNull (
						chatUser.getNewChatUserInfo ())
				) {

					requestContext.addError (
						"No info to approve");

					return nextResponder (
						transaction,
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
						userConsoleLogic.userRequired (
							transaction));

				// update chat user

				chatUser

					.setNewChatUserInfo (
						null);

				// send rejection

				sendRejection (
					transaction,
					userConsoleLogic.userRequired (
						transaction),
					chatUser,
					optionalFromNullable (
						chatUserInfo.getThreadId ()),
					messageParam);

				Responder responder =
					updateQueueItem (
						transaction,
						chatUser,
						userConsoleLogic.userRequired (
							transaction));

				transaction.commit ();

				requestContext.addNotice (
					"Rejection sent");

				return responder;

			}

		}

	}

	private
	void sendRejection (
			@NonNull Transaction parentTransaction,
			@NonNull UserRec myUser,
			@NonNull ChatUserRec chatUser,
			@NonNull Optional<Long> threadId,
			@NonNull String messageParam) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"sendRejection");

		) {

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
						transaction,
						messageParam);

				chatMessage =
					Optional.of (
						chatMessageHelper.insert (
							transaction,
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
					transaction,
					chatMessage.get ());

			} else {

				TextRec messageText =
					textHelper.findOrCreate (
						transaction,
						messageParam);

				message =
					chatSendLogic.sendMessageMagic (
						transaction,
						chatUser,
						threadId,
						messageText,
						commandHelper.findByCodeRequired (
							transaction,
							chat,
							"join_info"),
						serviceHelper.findByCodeRequired (
							transaction,
							chat,
							"system"),
						0l);

				chatMessage =
					optionalAbsent ();

			}

			chatHelpLogLogic.createChatHelpLogOut (
				transaction,
				chatUser,
				optionalAbsent (),
				optionalOf (
					myUser),
				message,
				chatMessage,
				messageParam,
				optionalOf (
					commandHelper.findByCodeRequired (
						transaction,
						chat,
						"join_info")));

		}

	}

	private
	Responder goRejectImage (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull PendingMode mode) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"goRejectImage");

		) {

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

			ChatUserRec chatUser =
				chatUserHelper.findFromContextRequired (
					transaction);

			ChatRec chat =
				chatUser.getChat ();

			ChatUserImageRec chatUserImage =
				chatUserLogic.chatUserPendingImage (
					transaction,
					chatUser,
					ChatUserImageType.valueOf (
						mode.toString ()));

			// checks involving database

			if (chatUserImage == null) {

				requestContext.addError (
					"No photo to approve");

				return nextResponder (
					transaction,
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
					userConsoleLogic.userRequired (
						transaction));

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
					textHelper.findOrCreate (
						transaction,
						messageParam);

				chatMessage =
					chatMessageHelper.insert (
						transaction,
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
						userConsoleLogic.userRequired (
							transaction))

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
					transaction,
					chatMessage);

			} else {

				// sms

				message =
					chatSendLogic.sendMessageMmsFree (
						transaction,
						chatUser,
						optionalAbsent (),
						messageParam,
						commandHelper.findByCodeRequired (
							transaction,
							chat,
							mode.commandCode ()),
						serviceHelper.findByCodeRequired (
							transaction,
							chat,
							"system"));

			}

			// log message sent

			chatHelpLogLogic.createChatHelpLogOut (
				transaction,
				chatUser,
				optionalAbsent (),
				optionalOf (
					userConsoleLogic.userRequired (
						transaction)),
				message,
				optionalFromNullable (
					chatMessage),
				messageParam,
				optionalOf (
					commandHelper.findByCodeRequired (
						transaction,
						chat,
						mode.commandCode ())));

			// clear queue item

			Responder responder =
				updateQueueItem (
					transaction,
					chatUser,
					userConsoleLogic.userRequired (
						transaction));

			// wrap up

			transaction.commit ();

			requestContext.addNotice (
				"Rejection sent");

			return responder;

		}

	}

	/**
	 * Expires the existing queue item associated with the chat user and
	 * creates a new one if there is still anything to approve.
	 */
	private
	Responder updateQueueItem (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec chatUser,
			@NonNull UserRec myUser) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"updateQueueItem");

		) {

			if (
				moreToApprove (
					transaction,
					chatUser)
			) {

				return responder (
					"chatUserPendingFormResponder");

			}

			queueLogic.processQueueItem (
				transaction,
				chatUser.getQueueItem (),
				myUser);

			chatUser

				.setQueueItem (
					null);

			return responder (
				"queueHomeResponder");

		}

	}

	private
	boolean moreToApprove (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec chatUser) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"moreToApprove");

		) {

			return (

				isNotNull (
					chatUser.getNewChatUserName ())

				|| isNotNull (
					chatUser.getNewChatUserInfo ())

				|| isNotNull (
					chatUserLogic.chatUserPendingImage (
						transaction,
						chatUser,
						ChatUserImageType.image))

				|| isNotNull (
					chatUserLogic.chatUserPendingImage (
						transaction,
						chatUser,
						ChatUserImageType.video))


				|| isNotNull (
					chatUserLogic.chatUserPendingImage (
						transaction,
						chatUser,
						ChatUserImageType.audio))

			);

		}

	}

	private
	Responder nextResponder (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec chatUser) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"nextResponder");

		) {

			return responder (
				ifThenElse (
					moreToApprove (
						transaction,
						chatUser),
					() -> "chatUserPendingFormResponder",
					() -> "queueHomeResponder"));

		}

	}

}
