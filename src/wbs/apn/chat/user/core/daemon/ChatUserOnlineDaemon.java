package wbs.apn.chat.user.core.daemon;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;
import wbs.apn.chat.contact.model.ChatMessageMethod;
import wbs.apn.chat.core.logic.ChatMiscLogic;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.ChatUserType;
import wbs.apn.chat.user.info.logic.ChatInfoLogic;
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

		return 30;

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
			database.beginReadOnly ();

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

		Date timestamp =
			new Date ();

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		ChatUserRec chatUser =
			chatUserHelper.find (chatUserId);

		ChatRec chat =
			chatUser.getChat ();

		// only process online users

		if (! chatUser.getOnline ())
			return;

		// see if they need logging off after the logoff time

		if (

			chatUser.getDeliveryMethod ()
				== ChatMessageMethod.sms

			&& (

				chatUser.getLastAction () == null

				|| (
					chatUser.getLastAction ().getTime ()
					+ chat.getTimeLogoff () * 1000
				) < timestamp.getTime ()

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

		long webLogoffTime =
			+ transaction.timestamp ().getTime ()
			- chat.getTimeWebLogoff () * 1000;

		if (

			chatUser.getDeliveryMethod ()
				!= ChatMessageMethod.sms

			&& chatUser.getLastMessagePoll ().getTime ()
				< webLogoffTime

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

			(chatUser.getLastSend () == null
				|| chatUser.getLastSend ().getTime ()
						+ chat.getTimeSend () * 1000
					< timestamp.getTime ())

			&& (chatUser.getLastReceive () == null
				|| chatUser.getLastReceive ().getTime ()
						+ chat.getTimeReceive () * 1000
					< timestamp.getTime ())

			&& (chatUser.getLastInfo () == null
				|| chatUser.getLastInfo ().getTime ()
						+ chat.getTimeInfo () * 1000
					< timestamp.getTime ())

			&& (chatUser.getLastPic () == null
				|| chatUser.getLastPic ().getTime ()
						+ chat.getTimeInfo () * 1000
					< timestamp.getTime ())

		) {

			log.info (
				stringFormat (
					"Sending info to user %s",
					objectManager.objectPathMini (
						chatUser)));

			chatInfoLogic.sendUserInfos (
				chatUser,
				1,
				null);

		}

		// then see if they need asking what their name is

		if (

			chatUser.getName () == null

			&& (chatUser.getLastNameHint () == null
				|| chatUser.getLastNameHint ().getTime ()
						+ chat.getTimeName () * 1000
					< timestamp.getTime ())

			&& chatUser.getLastJoin () != null

			&& chatUser.getLastJoin ().getTime ()
					+ chat.getTimeNameJoin () * 1000
				< timestamp.getTime ()

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

			chatUser.getChatUserImageList ().isEmpty ()

			&& (chatUser.getLastPicHint () == null
				|| chatUser.getLastPicHint ().getTime ()
						+ chat.getTimePicHint () * 1000
					< timestamp.getTime ())

			&& chatUser.getLastJoin () != null

			&& chatUser.getLastJoin ().getTime ()
					+ 15 * 60 * 1000
				< timestamp.getTime ()

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

			! chatUser.getChatUserImageList ().isEmpty ()

			&& (chatUser.getLastPicHint () == null
				|| chatUser.getLastPicHint ().getTime ()
						+ chat.getTimePicHint () * 1000
					< timestamp.getTime ())

			&& (chatUser.getLastPic () == null
				|| chatUser.getLastPic ().getTime ()
						+ chat.getTimePicHint () * 1000
					< timestamp.getTime ())

			&& chatUser.getLastJoin () != null

			&& chatUser.getLastJoin ().getTime ()
					+ 15 * 60 * 1000
				< timestamp.getTime ()

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
