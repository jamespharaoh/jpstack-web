package wbs.clients.apn.chat.ad.daemon;

import static wbs.framework.utils.etc.Misc.laterThan;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;

import com.google.common.base.Optional;

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
import wbs.framework.application.annotations.SingletonComponent;
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

	// dependencies

	@Inject
	ChatCreditLogic chatCreditLogic;

	@Inject
	ChatHelpTemplateObjectHelper chatHelpTemplateHelper;

	@Inject
	ChatUserDao chatUserDao;

	@Inject
	ChatUserObjectHelper chatUserHelper;

	@Inject
	ChatUserLogic chatUserLogic;

	@Inject
	Database database;

	@Inject
	ExceptionLogger exceptionLogger;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	ObjectManager objectManager;

	// prototype dependencies

	@Inject
	Provider<MessageSender> messageSender;

	// details

	@Override
	protected
	String getThreadName () {
		return "ChatAdultAd";
	}

	@Override
	protected
	int getDelayMs () {
		return 60 * 1000;
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
			int chatUserId) {

		log.debug (
			stringFormat (
				"Attempting to send adult ad to %s",
				chatUserId));

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		// find the user

		ChatUserRec chatUser =
			chatUserHelper.findOrNull (
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
			serviceHelper.findByCodeOrNull (
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
