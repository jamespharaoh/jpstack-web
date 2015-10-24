package wbs.clients.apn.chat.user.core.daemon;

import static wbs.framework.utils.etc.Misc.dateToInstant;
import static wbs.framework.utils.etc.Misc.earlierThan;
import static wbs.framework.utils.etc.Misc.instantToDate;
import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.List;

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
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.utils.RandomLogic;
import wbs.platform.daemon.SleepingDaemonService;

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
	ExceptionLogger exceptionLogger;

	@Inject
	RandomLogic randomLogic;

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
			database.beginReadOnly (
				this);

		List<ChatRec> chats =
			chatHelper.findAll ();

		transaction.close ();

		// then call doMonitorSwap for any whose time has come

		for (
			ChatRec chat
				: chats
		) {

			if (

				isNull (
					chat.getLastMonitorSwap ())

				|| earlierThan (
					dateToInstant (
						chat.getLastMonitorSwap ()
					).plus (
						chat.getTimeMonitorSwap () * 1000),
					transaction.now ())

			) {

				doMonitorSwap (
					chat.getId (),
					randomLogic.sample (
						Gender.values ()),
					randomLogic.sample (
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

			exceptionLogger.logThrowable (
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
			database.beginReadWrite (
				this);

		ChatRec chat =
			chatHelper.find (
				chatId);

		chat

			.setLastMonitorSwap (
				instantToDate (
					transaction.now ()));

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

		for (
			ChatUserRec monitor
				: allMonitors
		) {

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
			randomLogic.sample (
				onlineMonitors);

		monitor

			.setOnline (
				false);

		String tookOff =
			monitor.getCode ();

		// pick a random monitor to bring online

		monitor =
			randomLogic.sample (
				offlineMonitors);

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
