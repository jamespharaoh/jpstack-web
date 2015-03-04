package wbs.clients.apn.chat.user.core.daemon;

import static wbs.framework.utils.etc.Misc.pickRandom;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;
import wbs.clients.apn.chat.core.model.ChatObjectHelper;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.core.model.ChatUserType;
import wbs.clients.apn.chat.user.core.model.Gender;
import wbs.clients.apn.chat.user.core.model.Orient;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.daemon.SleepingDaemonService;
import wbs.platform.exception.logic.ExceptionLogic;

import com.google.common.base.Optional;

@Log4j
@SingletonComponent ("chatMonitorSwapDaemon")
public
class ChatMonitorSwapDaemon
	extends SleepingDaemonService {

	// dependencies

	@Inject
	ChatObjectHelper chatHelper;

	@Inject
	ChatUserObjectHelper chatUserHelper;

	@Inject
	Database database;

	@Inject
	ExceptionLogic exceptionLogic;

	@Inject
	Random random;

	// details

	@Override
	protected
	int getDelayMs () {
		return 10 * 1000;
	}

	@Override
	protected
	String generalErrorSource () {
		return "chat monitor swap daemon";
	}

	@Override
	protected
	String generalErrorSummary () {
		return "error swapping monitors";
	}

	@Override
	protected
	String getThreadName () {
		return "ChatMonitorSwap";
	}

	// implementation

	@Override
	protected
	void runOnce () {

		// get list of chats

		@Cleanup
		Transaction transaction =
			database.beginReadOnly ();

		List<ChatRec> chats =
			chatHelper.findAll ();

		transaction.close ();

		// then call doMonitorSwap for any whose time has come

		Date now =
			new Date ();

		for (ChatRec chat
				: chats) {

			if (chat.getLastMonitorSwap () == null
				|| chat.getLastMonitorSwap ().getTime ()
						+ chat.getTimeMonitorSwap () * 1000
					< now.getTime ()) {

				doMonitorSwap (
					chat.getId (),
					pickRandom (
						random,
						Gender.values ()),
					pickRandom (
						random,
						Orient.values ()));

			}

		}

	}

	void doMonitorSwap (
			int chatId,
			Gender gender,
			Orient orient) {

		try {

			doMonitorSwapReal (
				chatId,
				gender,
				orient);

		} catch (Exception exception) {

			exceptionLogic.logThrowable (
				"daemon",
				stringFormat (
					"chat %s %s %s",
					chatId,
					gender.toString (),
					orient.toString ()),
				exception,
				Optional.<Integer>absent (),
				false);

		}

	}

	void doMonitorSwapReal (
			int chatId,
			Gender gender,
			Orient orient) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		ChatRec chat =
			chatHelper.find (
				chatId);

		chat

			.setLastMonitorSwap (
				new Date ());

		// fetch all appropriate monitors

		List<ChatUserRec> allMonitors =
			chatUserHelper.find (
				chat,
				ChatUserType.monitor,
				orient,
				gender);

		// now sort into online and offline ones

		List<ChatUserRec> onlineMonitors =
			new ArrayList<ChatUserRec> ();

		List<ChatUserRec> offlineMonitors =
			new ArrayList<ChatUserRec> ();

		for (ChatUserRec monitor
				: allMonitors) {

			if (monitor.getOnline ()) {

				onlineMonitors.add (
					monitor);

			} else {

				offlineMonitors.add (
					monitor);

			}

		}

		if (onlineMonitors.size () == 0
				|| offlineMonitors.size () == 0)
			return;

		// pick a random monitor to take offline

		ChatUserRec monitor =
			onlineMonitors.get (
				random.nextInt (onlineMonitors.size ()));

		monitor

			.setOnline (
				false);

		String tookOff =
			monitor.getCode ();

		// pick a random monitor to bring online

		monitor =
			offlineMonitors.get (
				random.nextInt (
					offlineMonitors.size ()));

		monitor

			.setOnline (
				true);

		String putOn =
			monitor.getCode ();

		log.info (
			stringFormat (
				"Swapping %s %s monitor %s for %s",
				orient, gender,
				tookOff,
				putOn));

		transaction.commit ();

	}

}
