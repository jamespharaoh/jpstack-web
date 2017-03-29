package wbs.apn.chat.ad.daemon;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.time.TimeUtils.laterThan;

import java.util.List;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.joda.time.Duration;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
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
	String getThreadName () {
		return "ChatAdultAd";
	}

	@Override
	protected
	Duration getSleepDuration () {

		return Duration.standardSeconds (
			60);

	}

	@Override
	protected
	String generalErrorSource () {
		return "chat adult ad daemon";
	}

	@Override
	protected
	String generalErrorSummary () {
		return "error sending adult chat ads in background";
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
			"Looking for users to send an adult ad to");

		// get a list of users who have passed their ad time

		try (

			Transaction transaction =
				database.beginReadOnly (
					"ChatAdultAdDaemon.runOnce ()",
					this);

		) {

			List <ChatUserRec> chatUsers =
				chatUserHelper.findWantingAdultAd (
					transaction.now ());

			transaction.close ();

			// then call doAdultAd for each one

			for (
				ChatUserRec chatUser
					: chatUsers
			) {

				try {

					doAdultAd (
						taskLogger,
						chatUser.getId ());

				} catch (Exception exception) {

					exceptionLogger.logThrowable (
						taskLogger,
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

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"doAdultAd");

		taskLogger.debugFormat (
			"Attempting to send adult ad to %s",
			integerToDecimalString (
				chatUserId));

		try (

			Transaction transaction =
				database.beginReadWrite (
					"ChatAdultAdDaemon.doAdultAd (chatUserId)",
					this);

		) {

			// find the user

			ChatUserRec chatUser =
				chatUserHelper.findRequired (
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

				taskLogger.noticeFormat (
					"Skipping adult ad to %s (no adult ads on this service)",
					objectManager.objectPath (
						chatUser));

				chatUser

					.setNextAdultAd (
						null);

				transaction.commit ();

				return;

			}

			ChatUserRec userOnAdultService =
				chatUserDao.find (
					chat.getAdultAdsChat (),
					chatUser.getNumber ());

			if (
				userOnAdultService != null
				&& userOnAdultService.getFirstJoin () != null
			) {

				taskLogger.noticeFormat (
					"Skipping adult ad to %s (already on adult service)",
					objectManager.objectPath (
						chatUser));

				chatUser.setNextAdultAd (null);

				transaction.commit ();

				return;

			}

			ChatCreditCheckResult creditCheckResult =
				chatCreditLogic.userCreditCheck (
					taskLogger,
					chatUser);

			if (creditCheckResult.failed ()) {

				taskLogger.noticeFormat (
					"Skipping adult ad to %s (%s)",
					objectManager.objectPath (
						chatUser),
					creditCheckResult.details ());

				chatUser

					.setNextAdultAd (
						null);

				transaction.commit ();

				return;

			}

			if (chatUser.getFirstJoin () == null) {

				taskLogger.noticeFormat (
					"Skipping adult ad to %s (never fully joined)",
					objectManager.objectPath (
						chatUser));

				chatUser.setNextAdultAd (null);
				transaction.commit ();
				return;

			}

			if (
				! chatUser.getNumber ().getNumber ().startsWith ("447")
				|| chatUser.getNumber ().getNumber ().length () != 12
			) {

				taskLogger.noticeFormat (
					"Skipping adult ad to %s (not a mobile number)",
					objectManager.objectPath (
						chatUser));

				chatUser.setNextAdultAd (null);

				transaction.commit ();

				return;

			}

			// send the message

			String templateCode = null;

			if (chatUser.getOrient () == Orient.bi) {

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
					chat,
					"system",
					templateCode);

			taskLogger.noticeFormat (
				"Sending adult ad to %s: %s",
				objectManager.objectPath (
					chatUser),
				template.getText ());

			ChatSchemeRec adultScheme =
				chat.getAdultAdsChat ().getChatSchemes ().iterator ().next ();

			ServiceRec systemService =
				serviceHelper.findByCodeRequired (
					chat,
					"system");

			messageSender.get ()

				.number (
					chatUser.getNumber ())

				.messageString (
					taskLogger,
					template.getText ())

				.numFrom (
					adultScheme.getRbNumber ())

				.routerResolve (
					adultScheme.getRbFreeRouter ())

				.service (
					systemService)

				.send (
					taskLogger);

			// clear his next ad time

			chatUser.setNextAdultAd (null);

			transaction.commit ();

		}

	}

}
