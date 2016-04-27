package wbs.clients.apn.chat.user.core.daemon;

import static wbs.framework.utils.etc.Misc.earlierThan;
import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.isEmpty;
import static wbs.framework.utils.etc.Misc.isNotEmpty;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.Misc.moreThan;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;

import org.joda.time.Duration;
import org.joda.time.Instant;

import wbs.clients.apn.chat.contact.model.ChatMessageMethod;
import wbs.clients.apn.chat.core.logic.ChatMiscLogic;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.user.core.logic.ChatUserLogic;
import wbs.clients.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.core.model.ChatUserType;
import wbs.clients.apn.chat.user.info.logic.ChatInfoLogic;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.ObjectManager;
import wbs.platform.daemon.SleepingDaemonService;

@Log4j
@SingletonComponent ("chatUserOnlineDaemon")
public
class ChatUserOnlineDaemon
	extends SleepingDaemonService {

	// dependencies

	@Inject
	ChatInfoLogic chatInfoLogic;

	@Inject
	ChatMiscLogic chatMiscLogic;

	@Inject
	ChatUserLogic chatUserLogic;

	@Inject
	ChatUserObjectHelper chatUserHelper;

	@Inject
	Database database;

	@Inject
	ObjectManager objectManager;

	// details

	@Override
	protected
	int getDelayMs () {

		return 30 * 1000;

	}

	@Override
	protected
	String generalErrorSource () {

		return "chat user online daemon";

	}

	@Override
	protected
	String generalErrorSummary () {

		return "error checking for online users";

	}

	@Override
	protected
	String getThreadName () {

		return "ChatUserOnline";

	}

	// implementation

	@Override
	protected
	void runOnce () {

		log.debug ("Checking online users for action needed");

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				this);

		List<ChatUserRec> onlineUsers =
			chatUserHelper.findOnline (
				ChatUserType.user);

		transaction.close ();

		for (ChatUserRec chatUser
				: onlineUsers) {

			doUser (
				chatUser.getId ());

		}

	}

	void doUser (
			int chatUserId) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		ChatUserRec chatUser =
			chatUserHelper.find (chatUserId);

		ChatRec chat =
			chatUser.getChat ();

		// only process online users

		if (! chatUser.getOnline ())
			return;

		// see if they need logging off after the logoff time

		if (

			equal (
				chatUser.getDeliveryMethod (),
				ChatMessageMethod.sms)

			&& (

				isNull (
					chatUser.getLastAction ())

				|| earlierThan (
					chatUser.getLastAction ().plus (
						chat.getTimeLogoff () * 1000),
					transaction.now ())

			)

		) {

			log.info (
				stringFormat (
					"Automatically logging off user %s",
					chatUser.getCode ()));

			chatMiscLogic.userLogoffWithMessage (
				chatUser,
				null,
				true);

			transaction.commit ();

			return;

		}

		// see if they need logging off after the web logoff time

		Instant webLogoffTime =
			transaction.now ().minus (
				Duration.standardSeconds (
					chat.getTimeWebLogoff ()));

		if (

			chatUser.getDeliveryMethod ()
				!= ChatMessageMethod.sms

			&& earlierThan (
				chatUser.getLastMessagePoll (),
				webLogoffTime)

		) {

			log.info (
				stringFormat (
					"Automatically logging off %s user %s",
					chatUser.getDeliveryMethod (),
					chatUser.getCode ()));

			chatUserLogic.logoff (
				chatUser,
				true);

			transaction.commit ();

			return;

		}

		// see if they need logging off due to the session limit

		if (
			chatUser.getSessionInfoRemain () != null
			&& chatUser.getSessionInfoRemain () <= 0
		) {

			log.info (
				stringFormat (
					"Logging off %s user %s due to session info limit",
					chatUser.getDeliveryMethod (),
					chatUser.getCode ()));

			chatUserLogic.logoff (
				chatUser,
				true);

			transaction.commit ();

			return;

		}

		// below here is just for sms users

		if (chatUser.getDeliveryMethod () != ChatMessageMethod.sms)
			return;

		// ignore deleted users

		if (chatUser.getNumber () == null) {

			log.warn (
				stringFormat (
					"Logging off %s: no number",
					objectManager.objectPath (
						chatUser)));

			chatUser

				.setOnline (
					false);

			return;
		}

		// then see if they need a message from someone

		if (

			(

				isNull (
					chatUser.getLastSend ())

				|| earlierThan (
					chatUser.getLastSend ().plus (
						chat.getTimeSend () * 1000),
					transaction.now ())

			)

			&& (

				isNull (
					chatUser.getLastReceive ())

				|| earlierThan (
					chatUser.getLastReceive ().plus (
						chat.getTimeReceive () * 1000),
					transaction.now ())

			)

			&& (

				isNull (
					chatUser.getLastInfo ())

				|| earlierThan (
					chatUser.getLastInfo ().plus (
						chat.getTimeInfo () * 1000),
					transaction.now ()))

			&& (

				isNull (
					chatUser.getLastPic ())

				|| earlierThan (
					chatUser.getLastPic ().plus (
						chat.getTimeInfo () * 1000),
					transaction.now ()))

			&& (

				isNull (
					chatUser.getSessionInfoRemain ())

				|| moreThan (
					chatUser.getSessionInfoRemain (),
					1))

		) {

			log.info (
				stringFormat (
					"Sending info to user %s",
					objectManager.objectPathMini (
						chatUser)));

			int numSent =
				chatInfoLogic.sendUserInfos (
					chatUser,
					1,
					null);

			if (chatUser.getSessionInfoRemain () != null) {

				chatUser

					.setSessionInfoRemain (
						chatUser.getSessionInfoRemain () - numSent);

			}

		}

		// then see if they need asking what their name is

		if (

			isNull (
				chatUser.getName ())

			&& (

				isNull (
					chatUser.getLastNameHint ())

				|| earlierThan (
					chatUser.getLastNameHint ().plus (
						chat.getTimeName () * 1000),
					transaction.now ()))

			&& isNotNull (
				chatUser.getLastJoin ())

			&& earlierThan (
				chatUser.getLastJoin ().plus (
					chat.getTimeNameJoin () * 1000),
				transaction.now ())

		) {

			log.info (
				stringFormat (
					"Sending name hint to user %s",
					chatUser.getCode ()));

			chatInfoLogic.sendNameHint (
				chatUser);

		}

		// or a dating hint
		/*
		if ((chatUser.getDateMode() == null || chatUser.getDateMode() == ChatUserDateMode.none)
				&& (chatUser.getLastDateHint() == null || chatUser
						.getLastDateHint().getTime()
						+ 7 * 24 * 60 * 60 * 1000 < timestamp.getTime())
				&& chatUser.getLastJoin() != null
				&& chatUser.getLastJoin().getTime() + 5 * 60 * 1000 < timestamp
						.getTime()) {

			logger.info ("Sending date join hint to user "
					+ chatUser.getCode());

			chatDateLogic.chatUserDateJoinHint (chatUser);

		}*/

		// see if they need a picture hint

		if (

			isEmpty (
				chatUser.getChatUserImageList ())

			&& (

				isNull (
					chatUser.getLastPicHint ())

				|| earlierThan (
					chatUser.getLastPicHint ().plus (
						chat.getTimePicHint () * 1000),
					transaction.now ()))

			&& isNotNull (
				chatUser.getLastJoin ())

			&& earlierThan (
				chatUser.getLastJoin ().plus (
					15 * 60 * 1000),
				transaction.now ())

		) {

			log.info (
				stringFormat (
					"Sending pic hint to user %s",
					chatUser.getCode ()));

			chatInfoLogic.sendPicHint (
				chatUser);

		}

		// or another pic hint

		if (

			isNotEmpty (
				chatUser.getChatUserImageList ())

			&& (

				isNull (
					chatUser.getLastPicHint ())

				|| earlierThan (
					chatUser.getLastPicHint ().plus (
						chat.getTimePicHint () * 1000),
					transaction.now ()))

			&& (

				isNull (
					chatUser.getLastPic ())

				|| earlierThan (
					chatUser.getLastPic ().plus (
						chat.getTimePicHint () * 1000),
					transaction.now ()))

			&& isNotNull (
				chatUser.getLastJoin ())

			&& earlierThan (
				chatUser.getLastJoin ().plus (
					15 * 60 * 1000),
				transaction.now ())

		) {

			log.info (
				stringFormat (
					"Sending pic hint 2 to user %s",
					chatUser.getCode ()));

			chatInfoLogic.sendPicHint2 (
				chatUser);

		}

		transaction.commit ();

	}

}
