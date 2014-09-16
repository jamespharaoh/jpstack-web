package wbs.apn.chat.ad.daemon;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;
import wbs.apn.chat.bill.logic.ChatCreditLogic;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.help.model.ChatHelpTemplateObjectHelper;
import wbs.apn.chat.help.model.ChatHelpTemplateRec;
import wbs.apn.chat.scheme.model.ChatSchemeRec;
import wbs.apn.chat.user.core.model.ChatUserDao;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.Gender;
import wbs.apn.chat.user.core.model.Orient;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.ObjectManager;
import wbs.platform.daemon.SleepingDaemonService;
import wbs.platform.exception.logic.ExceptionLogic;
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
	ServiceObjectHelper serviceHelper;

	@Inject
	Database database;

	@Inject
	ExceptionLogic exceptionLogic;

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

		log.debug ("Looking for users to send an adult ad to");

		// get a list of users who have passed their ad time

		@Cleanup
		Transaction transaction =
			database.beginReadOnly ();

		List<ChatUserRec> chatUsers =
			chatUserHelper.findWantingAdultAd ();

		transaction.close ();

		// then call doAdultAd for each one

		for (ChatUserRec chatUser : chatUsers) {

			try {

				doAdultAd (chatUser.getId ());

			} catch (Exception exception) {

				exceptionLogic.logThrowable (
					"daemon",
					"ChatAdDaemon",
					exception,
					null,
					false);

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
			database.beginReadWrite ();

		// find the user

		ChatUserRec chatUser =
			chatUserHelper.find (chatUserId);

		ChatRec chat =
			chatUser.getChat ();

		// check he really is due an adult ad

		Date now =
			new Date ();

		if (chatUser.getNextAdultAd ().getTime () > now.getTime ())
			return;

		if (chat.getAdultAdsChat () == null) {

			log.info (
				stringFormat (
					"Skipping adult ad to %s (no adult ads on this service)",
					objectManager.objectPath (chatUser)));

			chatUser.setNextAdultAd (null);
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

		if (! chatCreditLogic.userCreditOk (chatUser, false)) {

			log.info (
				stringFormat (
					"Skipping adult ad to %s (failed credit check)",
					objectManager.objectPath (chatUser)));

			chatUser.setNextAdultAd (null);
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
			templateCode = "adult_ad_both";
		} else if (chatUser.likes (Gender.male)) {
			templateCode = "adult_ad_guys";
		} else if (chatUser.likes (Gender.female)) {
			templateCode = "adult_ad_girls";
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
			serviceHelper.findByCode (
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
