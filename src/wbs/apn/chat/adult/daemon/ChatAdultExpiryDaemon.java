package wbs.apn.chat.adult.daemon;

import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.time.TimeUtils.earlierThan;

import java.util.Collections;
import java.util.List;

import com.google.common.base.Optional;

import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import org.joda.time.Duration;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.ObjectManager;

import wbs.platform.daemon.SleepingDaemonService;

import wbs.apn.chat.contact.logic.ChatSendLogic;
import wbs.apn.chat.contact.logic.ChatSendLogic.TemplateMissing;
import wbs.apn.chat.scheme.model.ChatSchemeRec;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;

@Log4j
@SingletonComponent ("chatAdultExpiryDaemon")
public
class ChatAdultExpiryDaemon
	extends SleepingDaemonService {

	// singleton dependencies

	@SingletonDependency
	ChatSendLogic chatSendLogic;

	@SingletonDependency
	ChatUserObjectHelper chatUserHelper;

	@SingletonDependency
	Database database;

	@SingletonDependency
	ObjectManager objectManager;

	// details

	@Override
	protected
	Duration getSleepDuration () {

		return Duration.standardSeconds (
			30);

	}

	@Override
	protected
	String generalErrorSource () {

		return "chat adult expiry daemon";

	}

	@Override
	protected
	String generalErrorSummary () {

		return "error finding users for adult expiry";

	}

	@Override
	protected
	String getThreadName () {

		return "ChatAdultExpiry";

	}

	// implementation

	@Override
	protected
	void runOnce () {

		log.debug (
			"Checking for all users whose adult verification has expired");

		try (

			Transaction transaction =
				database.beginReadOnly (
					"ChatAdultExpiryDaemon.runOnce ()",
					this);

		) {

			List <ChatUserRec> chatUsers =
				chatUserHelper.findAdultExpiryLimit (
					transaction.now (),
					1000);

			transaction.close ();

			for (
				ChatUserRec chatUser
					: chatUsers
			) {

				doUserAdultExpiry (
					chatUser.getId ());

			}

		}

	}

	void doUserAdultExpiry (
			@NonNull Long chatUserId) {

		try (

			Transaction transaction =
				database.beginReadWrite (
					"ChatAdultExpiryDaemon.runOnce ()",
					this);

		) {

			ChatUserRec chatUser =
				chatUserHelper.findRequired (
					chatUserId);

			ChatSchemeRec chatScheme =
				chatUser.getChatScheme ();

			String chatUserPath =
				objectManager.objectPath (
					chatUser);

			// make sure adult expiry is set

			if (chatUser.getAdultExpiry () == null) {

				log.warn (
					stringFormat (
						"Skipped adult expiry for %s ",
						chatUserPath,
						"(field is null)"));

				return;

			}

			// make sure adult expiry time is in past

			if (
				earlierThan (
					transaction.now (),
					chatUser.getAdultExpiry ())
			) {

				log.warn (
					stringFormat (
						"Skipped adult expiry for %s ",
						chatUserPath,
						"(time is in future)"));

				return;

			}

			log.info (
				stringFormat (
					"Performing adult expiry for %s (time is %s)",
					chatUserPath,
					chatUser.getAdultExpiry ().toString ()));

			// update the user

			chatUser.setAdultVerified (false);
			chatUser.setAdultExpiry (null);

			if (chatUser.getBlockAll ()) {

				log.info (
					stringFormat (
						"Not sending adult expiry message to %s due to block all",
						chatUserPath));

			} else if (chatUser.getNumber () == null) {

				log.info (
					stringFormat (
						"Not sending adult expiry message to %s due to deletion",
						chatUserPath));

			} else if (chatUser.getChatScheme () == null) {

				log.info (
					stringFormat (
						"Not sending adult expiry message to %s ",
						chatUserPath,
						"due to lack of scheme"));

			} else {

				// send them a message

				if (chatScheme.getRbFreeRouter () != null) {

					chatSendLogic.sendSystemRbFree (
						chatUser,
						Optional.<Long>absent (),
						"adult_expiry",
						TemplateMissing.error,
						Collections.<String,String>emptyMap ());

				} else {

					log.warn (
						stringFormat (
							"Not sending adult expiry to %s as no route is ",
							chatUserPath,
							"configured"));

				}

			}

			transaction.commit ();

		}

	}

}
