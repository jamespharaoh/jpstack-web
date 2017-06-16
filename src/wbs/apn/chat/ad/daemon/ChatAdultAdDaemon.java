package wbs.apn.chat.ad.daemon;

import static wbs.utils.collection.CollectionUtils.iterableFirstElementRequired;
import static wbs.utils.etc.EnumUtils.enumEqualSafe;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.time.TimeUtils.laterThan;

import java.util.List;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectManager;

import wbs.platform.daemon.SleepingDaemonService;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;

import wbs.sms.message.outbox.logic.SmsMessageSender;

import wbs.apn.chat.bill.logic.ChatCreditCheckResult;
import wbs.apn.chat.bill.logic.ChatCreditLogic;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.help.model.ChatHelpTemplateObjectHelper;
import wbs.apn.chat.help.model.ChatHelpTemplateRec;
import wbs.apn.chat.scheme.model.ChatSchemeRec;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserDao;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.Gender;
import wbs.apn.chat.user.core.model.Orient;

@SingletonComponent ("chatAdultAdDaemon")
public
class ChatAdultAdDaemon
	extends SleepingDaemonService {

	// singleton dependencies

	@SingletonDependency
	ChatCreditLogic chatCreditLogic;

	@SingletonDependency
	ChatHelpTemplateObjectHelper chatHelpTemplateHelper;

	@SingletonDependency
	ChatUserDao chatUserDao;

	@SingletonDependency
	ChatUserObjectHelper chatUserHelper;

	@SingletonDependency
	ChatUserLogic chatUserLogic;

	@SingletonDependency
	Database database;

	@SingletonDependency
	ExceptionLogger exceptionLogger;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ServiceObjectHelper serviceHelper;

	@SingletonDependency
	ObjectManager objectManager;

	// prototype dependencies

	@PrototypeDependency
	Provider <SmsMessageSender> messageSender;

	// details

	@Override
	protected
	String friendlyName () {
		return "Chat user adult ads";
	}

	@Override
	protected
	String backgroundProcessName () {
		return "chat-user.adult-ads";
	}

	// implementation

	@Override
	protected
	void runOnce (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadOnly (
					logContext,
					parentTaskLogger,
					"ChatAdultAdDaemon.runOnce");

		) {

			transaction.debugFormat (
				"Looking for users to send an adult ad to");

			List <ChatUserRec> chatUsers =
				chatUserHelper.findWantingAdultAd (
					transaction,
					transaction.now ());

			transaction.close ();

			// then call doAdultAd for each one

			for (
				ChatUserRec chatUser
					: chatUsers
			) {

				try {

					doAdultAd (
						transaction,
						chatUser.getId ());

				} catch (Exception exception) {

					exceptionLogger.logThrowable (
						transaction,
						"daemon",
						"ChatAdDaemon",
						exception,
						Optional.absent (),
						GenericExceptionResolution.tryAgainLater);

				}

			}

		}

	}

	private
	void doAdultAd (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long chatUserId) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"doAdultAd");

		) {

			transaction.debugFormat (
				"Attempting to send adult ad to %s",
				integerToDecimalString (
					chatUserId));

			// find the user

			ChatUserRec chatUser =
				chatUserHelper.findRequired (
					transaction,
					chatUserId);

			ChatRec chat =
				chatUser.getChat ();

			// check he really is due an adult ad

			if (
				laterThan (
					chatUser.getNextAdultAd (),
					transaction.now ())
			) {
				return;
			}

			if (chat.getAdultAdsChat () == null) {

				transaction.noticeFormat (
					"Skipping adult ad to %s (no adult ads on this service)",
					objectManager.objectPath (
						transaction,
						chatUser));

				chatUser

					.setNextAdultAd (
						null);

				transaction.commit ();

				return;

			}

			Optional <ChatUserRec> userOnAdultService =
				chatUserDao.find (
					transaction,
					chat.getAdultAdsChat (),
					chatUser.getNumber ());

			if (

				optionalIsPresent (
					userOnAdultService)

				&& isNotNull (
					userOnAdultService.get ().getFirstJoin ())

			) {

				transaction.noticeFormat (
					"Skipping adult ad to %s (already on adult service)",
					objectManager.objectPath (
						transaction,
						chatUser));

				chatUser

					.setNextAdultAd (
						null)

				;

				transaction.commit ();

				return;

			}

			ChatCreditCheckResult creditCheckResult =
				chatCreditLogic.userCreditCheck (
					transaction,
					chatUser);

			if (creditCheckResult.failed ()) {

				transaction.noticeFormat (
					"Skipping adult ad to %s (%s)",
					objectManager.objectPath (
						transaction,
						chatUser),
					creditCheckResult.details ());

				chatUser

					.setNextAdultAd (
						null);

				transaction.commit ();

				return;

			}

			if (chatUser.getFirstJoin () == null) {

				transaction.noticeFormat (
					"Skipping adult ad to %s (never fully joined)",
					objectManager.objectPath (
						transaction,
						chatUser));

				chatUser.setNextAdultAd (null);

				transaction.commit ();

				return;

			}

			// send the message

			String templateCode = null;

			if (
				enumEqualSafe (
					chatUser.getOrient (),
					Orient.bi)
			) {

				templateCode =
					"adult_ad_both";

			} else if (
				chatUserLogic.likes (
					chatUser,
					Gender.male)
			) {

				templateCode =
					"adult_ad_guys";

			} else if (
				chatUserLogic.likes (
					chatUser,
					Gender.female)
			) {

				templateCode =
					"adult_ad_girls";

			}

			ChatHelpTemplateRec template =
				chatHelpTemplateHelper.findByTypeAndCode (
					transaction,
					chat,
					"system",
					templateCode);

			transaction.noticeFormat (
				"Sending adult ad to %s: %s",
				objectManager.objectPath (
					transaction,
					chatUser),
				template.getText ());

			ChatSchemeRec adultScheme =
				iterableFirstElementRequired (
					chat.getAdultAdsChat ().getChatSchemes ());

			ServiceRec systemService =
				serviceHelper.findByCodeRequired (
					transaction,
					chat,
					"system");

			messageSender.get ()

				.number (
					chatUser.getNumber ())

				.messageString (
					transaction,
					template.getText ())

				.numFrom (
					adultScheme.getRbNumber ())

				.routerResolve (
					transaction,
					adultScheme.getRbFreeRouter ())

				.service (
					systemService)

				.send (
					transaction);

			// clear his next ad time

			chatUser.setNextAdultAd (null);

			transaction.commit ();

		}

	}

}
