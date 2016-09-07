package wbs.clients.apn.chat.ad.daemon;

import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.TimeUtils.laterThan;

import java.util.List;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import org.joda.time.Duration;

import wbs.clients.apn.chat.bill.logic.ChatCreditCheckResult;
import wbs.clients.apn.chat.bill.logic.ChatCreditLogic;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.help.model.ChatHelpTemplateObjectHelper;
import wbs.clients.apn.chat.help.model.ChatHelpTemplateRec;
import wbs.clients.apn.chat.scheme.model.ChatSchemeRec;
import wbs.clients.apn.chat.user.core.logic.ChatUserLogic;
import wbs.clients.apn.chat.user.core.model.ChatUserDao;
import wbs.clients.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.core.model.Gender;
import wbs.clients.apn.chat.user.core.model.Orient;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.object.ObjectManager;
import wbs.platform.daemon.SleepingDaemonService;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.sms.message.outbox.logic.MessageSender;

@Log4j
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

	@SingletonDependency
	ServiceObjectHelper serviceHelper;

	@SingletonDependency
	ObjectManager objectManager;

	// prototype dependencies

	@PrototypeDependency
	Provider <MessageSender> messageSender;

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
	void runOnce () {

		log.debug (
			"Looking for users to send an adult ad to");

		// get a list of users who have passed their ad time

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				"ChatAdultAdDaemon.runOnce ()",
				this);

		List<ChatUserRec> chatUsers =
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
					chatUser.getId ());

			} catch (Exception exception) {

				exceptionLogger.logThrowable (
					"daemon",
					"ChatAdDaemon",
					exception,
					Optional.absent (),
					GenericExceptionResolution.tryAgainLater);

			}

		}

	}

	private
	void doAdultAd (
			@NonNull Long chatUserId) {

		log.debug (
			stringFormat (
				"Attempting to send adult ad to %s",
				chatUserId));

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ChatAdultAdDaemon.doAdultAd (chatUserId)",
				this);

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

			log.info (
				stringFormat (
					"Skipping adult ad to %s (no adult ads on this service)",
					objectManager.objectPath (
						chatUser)));

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

		if (userOnAdultService != null
				&& userOnAdultService.getFirstJoin () != null) {

			log.info (
				stringFormat (
					"Skipping adult ad to %s (already on adult service)",
					objectManager.objectPath (chatUser)));

			chatUser.setNextAdultAd (null);
			transaction.commit ();
			return;

		}

		ChatCreditCheckResult creditCheckResult =
			chatCreditLogic.userCreditCheck (
				chatUser);

		if (creditCheckResult.failed ()) {

			log.info (
				stringFormat (
					"Skipping adult ad to %s (%s)",
					objectManager.objectPath (
						chatUser),
					creditCheckResult.details ()));

			chatUser

				.setNextAdultAd (
					null);

			transaction.commit ();

			return;

		}

		if (chatUser.getFirstJoin () == null) {

			log.info (
				stringFormat (
					"Skipping adult ad to %s (never fully joined)",
					objectManager.objectPath (chatUser)));

			chatUser.setNextAdultAd (null);
			transaction.commit ();
			return;

		}

		if (! chatUser.getNumber ().getNumber ().startsWith ("447")
				|| chatUser.getNumber ().getNumber ().length () != 12) {

			log.info (
				stringFormat (
					"Skipping adult ad to %s (not a mobile number)",
					objectManager.objectPath (chatUser)));

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

		log.info (
			stringFormat (
				"Sending adult ad to %s: %s",
				objectManager.objectPath (chatUser),
				template.getText ()));

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
				template.getText ())

			.numFrom (
				adultScheme.getRbNumber ())

			.routerResolve (
				adultScheme.getRbFreeRouter ())

			.service (
				systemService)

			.send ();

		// clear his next ad time

		chatUser.setNextAdultAd (null);

		transaction.commit ();

	}

}
