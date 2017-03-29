package wbs.apn.chat.user.core.daemon;

import static wbs.utils.collection.CollectionUtils.collectionIsEmpty;
import static wbs.utils.collection.CollectionUtils.collectionIsNotEmpty;
import static wbs.utils.etc.EnumUtils.enumEqualSafe;
import static wbs.utils.etc.EnumUtils.enumName;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.NumberUtils.moreThanZero;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.time.TimeUtils.earlierThan;

import java.util.List;

import lombok.Cleanup;
import lombok.NonNull;

import org.joda.time.Duration;
import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectManager;

import wbs.platform.daemon.SleepingDaemonService;

import wbs.apn.chat.contact.model.ChatMessageMethod;
import wbs.apn.chat.core.logic.ChatMiscLogic;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.ChatUserType;
import wbs.apn.chat.user.info.logic.ChatInfoLogic;

@SingletonComponent ("chatUserOnlineDaemon")
public
class ChatUserOnlineDaemon
	extends SleepingDaemonService {

	// singleton dependencies

	@SingletonDependency
	ChatInfoLogic chatInfoLogic;

	@SingletonDependency
	ChatMiscLogic chatMiscLogic;

	@SingletonDependency
	ChatUserLogic chatUserLogic;

	@SingletonDependency
	ChatUserObjectHelper chatUserHelper;

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

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
	void runOnce (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"runOnce ()");

		taskLogger.debugFormat (
			"Checking online users for action needed");

		try (

			Transaction transaction =
				database.beginReadOnly (
					"ChatUserOnlineDaemon.runOnce ()",
					this);

		) {

			List <ChatUserRec> onlineUsers =
				chatUserHelper.findOnline (
					ChatUserType.user);

			transaction.close ();

			for (
				ChatUserRec chatUser
					: onlineUsers
			) {

				doUser (
					taskLogger,
					chatUser.getId ());

			}

		}

	}

	void doUser (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long chatUserId) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"doUser");

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ChatUserOnlineDaemon.doUser (chatUserId)",
				this);

		ChatUserRec chatUser =
			chatUserHelper.findRequired (
				chatUserId);

		ChatRec chat =
			chatUser.getChat ();

		// only process online users

		if (! chatUser.getOnline ())
			return;

		// see if they need logging off after the logoff time

		if (

			enumEqualSafe (
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

			taskLogger.noticeFormat (
				"Automatically logging off user %s",
				chatUser.getCode ());

			chatMiscLogic.userLogoffWithMessage (
				taskLogger,
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

			taskLogger.noticeFormat (
				"Automatically logging off %s user %s",
				enumName (
					chatUser.getDeliveryMethod ()),
				chatUser.getCode ());

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

			taskLogger.noticeFormat (
				"Logging off %s user %s due to session info limit",
				enumName (
					chatUser.getDeliveryMethod ()),
				chatUser.getCode ());

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

			taskLogger.warningFormat (
				"Logging off %s: no number",
				objectManager.objectPath (
					chatUser));

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

				|| moreThanZero (
					chatUser.getSessionInfoRemain ())

			)

		) {

			taskLogger.noticeFormat (
				"Sending info to user %s",
				objectManager.objectPathMini (
					chatUser));

			long numSent =
				chatInfoLogic.sendUserInfos (
					taskLogger,
					chatUser,
					1l,
					optionalAbsent ());

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

			taskLogger.noticeFormat (
				"Sending name hint to user %s",
				chatUser.getCode ());

			chatInfoLogic.sendNameHint (
				taskLogger,
				chatUser);

		}

		// or a dating hint
		/*
		if (

			(

				chatUser.getDateMode () == null

				|| chatUser.getDateMode () == ChatUserDateMode.none

			)

			&& (

				chatUser.getLastDateHint() == null

				|| (
					chatUser.getLastDateHint ().getTime ()
					+ 7 * 24 * 60 * 60 * 1000
				) < timestamp.getTime ()

			)

			&& chatUser.getLastJoin () != null

			&& chatUser.getLastJoin ().getTime () + 5 * 60 * 1000
				< timestamp.getTime ()

		) {

			logger.info ("Sending date join hint to user "
					+ chatUser.getCode());

			chatDateLogic.chatUserDateJoinHint (chatUser);

		}
		*/

		// see if they need a picture hint

		if (

			collectionIsEmpty (
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

			taskLogger.noticeFormat (
				"Sending pic hint to user %s",
				chatUser.getCode ());

			chatInfoLogic.sendPicHint (
				taskLogger,
				chatUser);

		}

		// or another pic hint

		if (

			collectionIsNotEmpty (
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

			taskLogger.noticeFormat (
				"Sending pic hint 2 to user %s",
				chatUser.getCode ());

			chatInfoLogic.sendPicHint2 (
				taskLogger,
				chatUser);

		}

		transaction.commit ();

	}

}
