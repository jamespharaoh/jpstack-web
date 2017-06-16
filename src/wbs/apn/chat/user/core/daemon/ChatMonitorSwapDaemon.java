package wbs.apn.chat.user.core.daemon;

import static wbs.utils.collection.IterableUtils.iterableFilter;
import static wbs.utils.collection.IterableUtils.iterableMapToList;
import static wbs.utils.etc.EnumUtils.enumName;
import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.string.StringUtils.keyEqualsDecimalInteger;
import static wbs.utils.string.StringUtils.keyEqualsEnum;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.time.TimeUtils.earlierThan;

import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
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
	String friendlyName () {
		return "Chat monitor swap";
	}

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

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"runOnce");

		) {

			// get list of chats

			List <Long> chatIds =
				getChatIds (
					taskLogger);

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
	List <Long> getChatIds (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadOnly (
					logContext,
					parentTaskLogger,
					"getChatIds");

		) {

			return iterableMapToList (
				iterableFilter (
					chat ->
						chatNeedsMonitorSwap (
							transaction,
							chat),
					chatHelper.findNotDeleted (
						transaction)),
				ChatRec::getId);

		}

	}

	private
	boolean chatNeedsMonitorSwap (
			@NonNull Transaction parentTransaction,
			@NonNull ChatRec chat) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"chatNeedsMonitorSwap");

		) {

			return (

				isNull (
					chat.getLastMonitorSwap ())

				|| earlierThan (
					chat.getLastMonitorSwap ().plus (
						chat.getTimeMonitorSwap () * 1000),
					transaction.now ())

			);

		}

	}

	private
	void doMonitorSwap (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long chatId,
			@NonNull Gender gender,
			@NonNull Orient orient) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"doMonitorSwap");

		) {

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

	}

	private
	void doMonitorSwapReal (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long chatId,
			@NonNull Gender gender,
			@NonNull Orient orient) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"doMonitorSwapReal",
					keyEqualsDecimalInteger (
						"chatId",
						chatId),
					keyEqualsEnum (
						"gender",
						gender),
					keyEqualsEnum (
						"orient",
						orient));

		) {

			ChatRec chat =
				chatHelper.findRequired (
					transaction,
					chatId);

			if (
				! chatNeedsMonitorSwap (
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
					transaction,
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

			if (
				onlineMonitors.size () == 0
				|| offlineMonitors.size () == 0
			) {
				return;
			}

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

			transaction.noticeFormat (
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
