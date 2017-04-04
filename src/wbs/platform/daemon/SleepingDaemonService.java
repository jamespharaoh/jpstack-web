package wbs.platform.daemon;

import static wbs.utils.collection.CollectionUtils.collectionHasTwoElements;
import static wbs.utils.collection.CollectionUtils.listFirstElementRequired;
import static wbs.utils.collection.CollectionUtils.listSecondElementRequired;
import static wbs.utils.etc.EnumUtils.enumName;
import static wbs.utils.etc.LogicUtils.booleanNotEqual;
import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.lessThan;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.hyphenToCamel;
import static wbs.utils.string.StringUtils.hyphenToUnderscore;
import static wbs.utils.string.StringUtils.joinWithoutSeparator;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringNotEqualSafe;
import static wbs.utils.string.StringUtils.stringSplitFullStop;
import static wbs.utils.time.TimeUtils.earlierThan;
import static wbs.utils.time.TimeUtils.millisecondsToDuration;

import java.util.List;

import lombok.NonNull;

import org.joda.time.Duration;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogEvent;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectManager;

import wbs.platform.background.model.BackgroundProcessObjectHelper;
import wbs.platform.background.model.BackgroundProcessRec;
import wbs.platform.object.core.model.ObjectTypeObjectHelper;
import wbs.platform.object.core.model.ObjectTypeRec;

import wbs.utils.random.RandomLogic;
import wbs.utils.string.FormatWriter;
import wbs.utils.string.StringFormatWriter;

public abstract
class SleepingDaemonService
	extends AbstractDaemonService {

	// singleton dependencies

	@SingletonDependency
	BackgroundProcessObjectHelper backgroundProcessHelper;

	@SingletonDependency
	Database database;

	@SingletonDependency
	ExceptionLogger exceptionLogger;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	ObjectTypeObjectHelper objectTypeHelper;

	@SingletonDependency
	RandomLogic randomLogic;

	// state

	String parentTypeCode;
	String backgroundProcessCode;
	Long backgroundProcessId;
	Duration backgroundProcessFrequency;
	Boolean backgroundProcessDebugEnabled;

	String runningToken;

	// hooks to override

	abstract protected
	String backgroundProcessName ();

	abstract protected
	void runOnce (
			TaskLogger parentTaskLogger);

	// details

	@Override
	protected
	String getThreadName () {

		return joinWithoutSeparator (
			hyphenToCamel (
				parentTypeCode),
			capitalise (
				hyphenToCamel (
					backgroundProcessCode)));

	}

	// life cycle

	@NormalLifecycleSetup
	public
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"setup");

		List <String> backgroundProcessNameParts =
			stringSplitFullStop (
				backgroundProcessName ());

		if (
			! collectionHasTwoElements (
				backgroundProcessNameParts)
		) {
			throw new RuntimeException ();
		}

		parentTypeCode =
			listFirstElementRequired (
				backgroundProcessNameParts);

		backgroundProcessCode =
			listSecondElementRequired (
				backgroundProcessNameParts);

		try (

			Transaction transaction =
				database.beginReadOnly (
					"SleepingDaemonService.setup ()",
					this);
		) {

			ObjectTypeRec parentType =
				objectTypeHelper.findByCodeRequired (
					GlobalId.root,
					hyphenToUnderscore (
						parentTypeCode));

			BackgroundProcessRec backgroundProcess =
				backgroundProcessHelper.findByCodeRequired (
					parentType,
					hyphenToUnderscore (
						backgroundProcessCode));

			backgroundProcessId =
				backgroundProcess.getId ();

			backgroundProcessDebugEnabled =
				backgroundProcess.getDebug ();

			backgroundProcessFrequency =
				backgroundProcess.getFrequency ();

			taskLogger.noticeFormat (
				"Found background process %s with id %s",
				backgroundProcessName (),
				integerToDecimalString (
					backgroundProcess.getId ()));

		}

	}

	// implementation

	@Override
	final protected
	void runService () {

		// work out initial delay

		Duration delay =
			calculateFirstDelay ();

		for (;;) {

			// delay

			try {

				Thread.sleep (
					delay.getMillis ());

			} catch (InterruptedException exception) {

				return;

			}

			// run service hook

			TaskLogger taskLogger =
				logContext.createTaskLogger (
					"runService ()",
					backgroundProcessDebugEnabled);

			try {

				if (
					! setBackgroundProcessStart (
						taskLogger)
				) {
					continue;
				}

				taskLogger.wrap (
					this::runOnce);

			} catch (Exception exception) {

				String errorSummary =
					stringFormat (
						"Error running background process %s",
						backgroundProcessName ());

				taskLogger.errorFormatException (
					exception,
					"%s",
					errorSummary);

				exceptionLogger.logThrowableWithSummary (
					taskLogger,
					"daemon",
					backgroundProcessName (),
					errorSummary,
					exception,
					optionalAbsent (),
					GenericExceptionResolution.tryAgainLater);

			} finally {

				if (
					isNotNull (
						runningToken)
				) {

					setBackgroundProcessStop (
						taskLogger);

				}

			}

			// work out next delay

			delay =
				calculateSubsequentDelay ();

		}

	}

	private
	Duration calculateFirstDelay () {

		return millisecondsToDuration (
			randomLogic.randomInteger (
				backgroundProcessFrequency.getMillis ()));

	}

	private
	Duration calculateSubsequentDelay () {

		if (
			lessThan (
				backgroundProcessFrequency.getMillis (),
				5000l)
		) {

			return backgroundProcessFrequency;

		} else {

			return millisecondsToDuration (
				backgroundProcessFrequency.getMillis () / 2).plus (
					randomLogic.randomInteger (
						backgroundProcessFrequency.getMillis ()));

		}

	}

	private
	boolean setBackgroundProcessStart (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"setBackgroundProcessStart");

		try (

			Transaction transaction =
				database.beginReadWrite (
					"SleepingDaemonService.setBackgroundProcessStart",
					this);

		) {

			BackgroundProcessRec backgroundProcess =
				backgroundProcessHelper.findRequired (
					backgroundProcessId);

			if (
				booleanNotEqual (
					backgroundProcess.getDebug (),
					backgroundProcessDebugEnabled)
			) {

				backgroundProcessDebugEnabled =
					backgroundProcess.getDebug ();

				return false;

			}

			if (

				backgroundProcess.getRunning ()

				&& earlierThan (
					transaction.now ().minus (
						Duration.standardMinutes (10)),
					backgroundProcess.getRunningWatchdogTime ())

			) {
				return false;
			}

			runningToken =
				randomLogic.generateLowercase (10);

			backgroundProcess

				.setRunning (
					true)

				.setRunningToken (
					runningToken)

				.setRunningStartTime (
					transaction.now ())

				.setRunningWatchdogTime (
					transaction.now ())

			;

			transaction.commit ();

			return true;

		}

	}

	private
	void setBackgroundProcessStop (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"setBackgroundProcessStop");

		try (

			Transaction transaction =
				database.beginReadWrite (
					"SleepingDaemonService.setBackgroundProcessStop",
					this);

		) {

			BackgroundProcessRec backgroundProcess =
				backgroundProcessHelper.findRequired (
					backgroundProcessId);

			if (

				! backgroundProcess.getRunning ()

				|| stringNotEqualSafe (
					backgroundProcess.getRunningToken (),
					runningToken)

			) {
				return;
			}

			backgroundProcess

				.setNumConsecutiveFailures (
					ifThenElse (
						taskLogger.findRoot ().errors (),
						() -> 1l +
							backgroundProcess.getNumConsecutiveFailures (),
						() -> 0l))

				.setLastRunTime (
					backgroundProcess.getRunningStartTime ())

				.setLastRunSuccess (
					! taskLogger.findRoot ().errors ())

				.setLastRunDuration (
					new Duration (
						backgroundProcess.getRunningStartTime (),
						transaction.now ()))

				.setLastTaskLog (
					taskLog (
						taskLogger.findRoot ()))

				.setRunning (
					false)

				.setRunningToken (
					null)

				.setRunningStartTime (
					null)

				.setRunningWatchdogTime (
					null)

			;

			transaction.commit ();

		} finally {

			runningToken = null;

		}

	}

	private
	String taskLog (
			@NonNull TaskLogEvent taskLogEvent) {

		StringFormatWriter formatWriter =
			new StringFormatWriter ();

		writeTaskLog (
			formatWriter,
			taskLogEvent);

		return formatWriter.toString ();

	}

	private
	void writeTaskLog (
			@NonNull FormatWriter formatWriter,
			@NonNull TaskLogEvent taskLogEvent) {

		formatWriter.writeLineFormatIncreaseIndent (
			"%s %s",
			enumName (
				taskLogEvent.eventSeverity ()),
			taskLogEvent.eventText ());

		taskLogEvent.eventChildren ().forEach (
			childEvent ->
				writeTaskLog (
					formatWriter,
					childEvent));

		formatWriter.decreaseIndent ();

	}

}