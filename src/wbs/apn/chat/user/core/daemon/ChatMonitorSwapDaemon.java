package wbs.apn.chat.user.core.daemon;

import static wbs.utils.collection.IterableUtils.iterableFilter;
import static wbs.utils.collection.IterableUtils.iterableMapToList;
import static wbs.utils.etc.EnumUtils.enumName;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.string.StringUtils.joinWithCommaAndSpace;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.time.TimeUtils.earlierThan;

import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
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

import wbs.utils.random.RandomLogic;

import wbs.apn.chat.core.model.ChatObjectHelper;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.ChatUserType;
import wbs.apn.chat.user.core.model.Gender;
import wbs.apn.chat.user.core.model.Orient;

@SingletonComponent ("chatMonitorSwapDaemon")
public
class ChatMonitorSwapDaemon
	extends SleepingDaemonService {

	// singleton dependencies

	@SingletonDependency
	ChatObjectHelper chatHelper;

	@SingletonDependency
	ChatUserObjectHelper chatUserHelper;

	@SingletonDependency
	Database database;

	@SingletonDependency
	ExceptionLogger exceptionLogger;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	RandomLogic randomLogic;

	// details

	@Override
	protected
	String backgroundProcessName () {
		return "chat.monitor-swap";
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

		// get list of chats

		try (

			Transaction transaction =
				database.beginReadOnly (
					taskLogger,
					"ChatMonitorSwapDaemon.runOnce ()",
					this);

		) {

			List <Long> chatIds =
				iterableMapToList (
					ChatRec::getId,
					iterableFilter (
						chat ->
							chatNeedsMonitorSwap (
								taskLogger,
								transaction,
								chat),
						chatHelper.findNotDeleted ()));

			transaction.close ();

			// then call doMonitorSwap for any whose time has come

			chatIds.forEach (
				chatId ->
					doMonitorSwap (
						taskLogger,
						chatId,
						randomLogic.sample (
							Gender.values ()),
						randomLogic.sample (
							Orient.values ())));

		}

	}

	private
	boolean chatNeedsMonitorSwap (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Transaction transaction,
			@NonNull ChatRec chat) {

		return (

			isNull (
				chat.getLastMonitorSwap ())

			|| earlierThan (
				chat.getLastMonitorSwap ().plus (
					chat.getTimeMonitorSwap () * 1000),
				transaction.now ())

		);

	}

	private
	void doMonitorSwap (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long chatId,
			@NonNull Gender gender,
			@NonNull Orient orient) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"doMonitorSwap");

		try {

			doMonitorSwapReal (
				taskLogger,
				chatId,
				gender,
				orient);

		} catch (Exception exception) {

			exceptionLogger.logThrowable (
				taskLogger,
				"daemon",
				stringFormat (
					"chat %s %s %s",
					integerToDecimalString (
						chatId),
					enumName (
						gender),
					enumName (
						orient)),
				exception,
				optionalAbsent (),
				GenericExceptionResolution.tryAgainLater);

		}

	}

	void doMonitorSwapReal (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long chatId,
			@NonNull Gender gender,
			@NonNull Orient orient) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"doMonitorSwapReal");

		try (

			Transaction transaction =
				database.beginReadWrite (
					taskLogger,
					stringFormat (
						"%s.%s (%s)",
						"ChatMonitorSwapDaemon",
						"doMonitorSwapReal",
						joinWithCommaAndSpace (
							stringFormat (
								"chatId = %s",
								integerToDecimalString (
									chatId)),
							stringFormat (
								"gender = %s",
								enumName (
									gender)),
							stringFormat (
								"orient = %s",
								enumName (
									orient)))),
					this);

		) {

			ChatRec chat =
				chatHelper.findRequired (
					chatId);

			if (
				! chatNeedsMonitorSwap (
					taskLogger,
					transaction,
					chat)
			) {
				return;
			}

			chat

				.setLastMonitorSwap (
					transaction.now ());

			// fetch all appropriate monitors

			List <ChatUserRec> allMonitors =
				chatUserHelper.find (
					chat,
					ChatUserType.monitor,
					orient,
					gender);

			// now sort into online and offline ones

			List <ChatUserRec> onlineMonitors =
				new ArrayList<> ();

			List <ChatUserRec> offlineMonitors =
				new ArrayList<> ();

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

			taskLogger.noticeFormat (
				"Swapping %s %s monitor %s for %s",
				enumName (
					orient),
				enumName (
					gender),
				tookOff,
				putOn);

			transaction.commit ();

		}

	}

}
