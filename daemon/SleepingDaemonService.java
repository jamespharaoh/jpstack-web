package wbs.platform.daemon;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.time.TimeUtils.millisecondsToDuration;

import lombok.NonNull;

import org.joda.time.Duration;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectManager;

import wbs.platform.background.logic.BackgroundLogic;
import wbs.platform.background.logic.BackgroundProcessHelper;
import wbs.platform.object.core.model.ObjectTypeObjectHelper;

import wbs.utils.random.RandomLogic;

public abstract
class SleepingDaemonService
	extends AbstractDaemonService {

	// singleton dependencies

	@SingletonDependency
	BackgroundLogic backgroundLogic;

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

	BackgroundProcessHelper helper;

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
		return helper.threadName ();
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

		helper =
			backgroundLogic.registerBackgroundProcess (
				taskLogger,
				backgroundProcessName (),
				this);

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
					helper.debugEnabled ());

			try {

				if (
					! helper.setBackgroundProcessStart (
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

				helper.setBackgroundProcessStop (
					taskLogger);

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
				helper.frequency ().getMillis ()));

	}

	private
	Duration calculateSubsequentDelay () {

		return millisecondsToDuration (
			helper.frequency ().getMillis () * 3 / 4).plus (
				randomLogic.randomInteger (
					helper.frequency ().getMillis () / 2));

	}

}