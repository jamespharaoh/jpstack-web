package wbs.apn.chat.ad.daemon;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.time.TimeUtils.laterThan;

import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.IdObject;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectManager;

import wbs.platform.daemon.SleepingDaemonService;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextRec;

import wbs.sms.command.model.CommandObjectHelper;

import wbs.utils.random.RandomLogic;

import wbs.apn.chat.ad.model.ChatAdTemplateRec;
import wbs.apn.chat.bill.logic.ChatCreditCheckResult;
import wbs.apn.chat.bill.logic.ChatCreditLogic;
import wbs.apn.chat.contact.logic.ChatSendLogic;
import wbs.apn.chat.core.logic.ChatMiscLogic;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserDao;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.Gender;
import wbs.apn.chat.user.core.model.Orient;

@SingletonComponent ("chatAdDaemon")
public
class ChatAdDaemon
	extends SleepingDaemonService {

	// singleton dependencies

	@SingletonDependency
	ChatCreditLogic chatCreditLogic;

	@SingletonDependency
	ChatMiscLogic chatLogic;

	@SingletonDependency
	ChatSendLogic chatSendLogic;

	@SingletonDependency
	ChatUserDao chatUserDao;

	@SingletonDependency
	ChatUserObjectHelper chatUserHelper;

	@SingletonDependency
	ChatUserLogic chatUserLogic;

	@SingletonDependency
	CommandObjectHelper commandHelper;

	@SingletonDependency
	Database database;

	@SingletonDependency
	ExceptionLogger exceptionLogger;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	RandomLogic randomLogic;

	@SingletonDependency
	ServiceObjectHelper serviceHelper;

	@SingletonDependency
	TextObjectHelper textHelper;

	// details

	@Override
	protected
	String backgroundProcessName () {
		return "chat.ad-sender";
	}

	// implementation

	@Override
	protected
	void runOnce (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"runOnce");

		taskLogger.debugFormat (
			"Looking for users to send an ad to");

		// get a list of users who have passed their ad time

		try (

			Transaction transaction =
				database.beginReadOnly (
					"ChatAdDaemon.runOnce ()",
					this);

		) {

			List <ChatUserRec> chatUsers =
				chatUserHelper.findWantingAd (
					transaction.now ());

			transaction.close ();

			// then call doChatUserAd for each one

			for (
				ChatUserRec chatUser
					: chatUsers
			) {

				try {

					doChatUserAd (
						taskLogger,
						chatUser.getId ());

				} catch (Exception exception) {

					exceptionLogger.logThrowable (
						taskLogger,
						"daemon",
						"ChatAdDaemon",
						exception,
						optionalAbsent (),
						GenericExceptionResolution.tryAgainLater);

				}

			}

		}

	}

	private
	void doChatUserAd (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long chatUserId) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"doChatUserAd");

		taskLogger.debugFormat (
			"Attempting to send ad to %s",
			integerToDecimalString (
				chatUserId));

		try (

			Transaction transaction =
				database.beginReadWrite (
					"ChatAdDaemon.doChatUserAd",
					this);

		) {

			// find the user

			ChatUserRec chatUser =
				chatUserHelper.findRequired (
					chatUserId);

			ChatRec chat =
				chatUser.getChat ();

			// check he really is due an ad

			if (
				laterThan (
					chatUser.getNextAd (),
					transaction.now ())
			) {
				return;
			}

			// do a credit and number check

			ChatCreditCheckResult creditCheckResult =
				chatCreditLogic.userCreditCheck (
					taskLogger,
					chatUser);

			if (
				creditCheckResult.failed ()
			) {

				taskLogger.noticeFormat (
					"Skipping ad to %s (%s)",
					objectManager.objectPath (chatUser),
					creditCheckResult.details ());

			} else if (chatUser.getFirstJoin () == null) {

				taskLogger.noticeFormat (
					"Skipping ad to %s (never fully joined)",
					objectManager.objectPath (
						chatUser));

			} else if (
				! chatUser.getNumber ().getNumber ().startsWith ("447")
				|| chatUser.getNumber ().getNumber ().length () != 12
			) {

				taskLogger.noticeFormat (
					"Skipping ad to %s (not a mobile number)",
					objectManager.objectPath (chatUser));

			} else {

				// work out credit

				long approvedCredit =
					+ chatUser.getCredit ()
					- chatUser.getCreditPending ()
					- chatUser.getCreditPendingStrict ();

				// pick an ad

				List<ChatAdTemplateRec> adTemplates =
					new ArrayList<ChatAdTemplateRec> (
						chatUser.getChat ().getChatAdTemplates ());

				TextRec text = null;

				while (! adTemplates.isEmpty ()) {

					int templateNumber =
						randomLogic.randomJavaInteger (
							adTemplates.size ());

					ChatAdTemplateRec chatAdTemplate =
						adTemplates.get (templateNumber);

					// pick the text

					if (chatUser.getOrient () == Orient.gay
							&& chatUser.getGender () == Gender.male) {

						text =
							chatAdTemplate.getGayMaleText ();

					} else if (chatUser.getOrient () == Orient.gay
							&& chatUser.getGender () == Gender.female) {

						text =
							chatAdTemplate.getGayFemaleText ();

					} else {

						text =
							chatAdTemplate.getGenericText ();

					}

					// check for the place holder

					boolean hasCreditPlaceholder =
						text.getText ().contains ("{credit}");

					boolean wantCreditPlaceholder =
						approvedCredit > 0;

					if (hasCreditPlaceholder == wantCreditPlaceholder)
						break;

					adTemplates.remove (templateNumber);

				}

				if (text == null) {

					taskLogger.noticeFormat (
						"Skipping ad to %s (no suitable ads configured)",
						objectManager.objectPath (
							chatUser));

				} else {

					// replace placeholder with credit

					String messageString =
						text.getText ().replace (
							"{credit}",
							String.format (
								"%d.%02d",
								approvedCredit / 100,
								approvedCredit % 100));

					// send the message

					taskLogger.noticeFormat (
						"Sending ad to %s: %s",
						objectManager.objectPath (
							chatUser),
						messageString);

					TextRec messageText =
						textHelper.findOrCreate (
							taskLogger,
							messageString);

					ServiceRec adService =
						serviceHelper.findByCodeRequired (
							chat,
							"ad");

					chatSendLogic.sendMessageMagic (
						taskLogger,
						chatUser,
						optionalAbsent (),
						messageText,
						commandHelper.findByCodeRequired (
							chat,
							"magic"),
						adService,
						IdObject.objectId (
							commandHelper.findByCodeRequired (
								chat,
								"join_next")));

				}

			}

			// set his next ad time

			chatUserLogic.scheduleAd (chatUser);

			transaction.commit ();

		}

	}

}
