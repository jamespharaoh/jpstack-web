package wbs.platform.daemon;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;

import org.apache.log4j.Logger;
import org.joda.time.Duration;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.utils.random.RandomLogic;

public abstract
class SleepingDaemonService
	extends AbstractDaemonService {

	// singleton dependencies

	@SingletonDependency
	ExceptionLogger exceptionLogger;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RandomLogic randomLogic;

	// hooks to override

	abstract protected
	Duration getSleepDuration ();

	abstract protected
	void runOnce (
			TaskLogger parentTaskLogger);

	abstract protected
	String generalErrorSource ();

	abstract protected
	String generalErrorSummary ();

	// implementation

	@Override
	final protected
	void runService () {

		// work out initial delay

		Duration delay =
			Duration.millis (
				randomLogic.randomInteger (
					getSleepDuration ().getMillis ()));

		while (true) {

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
					"runService ()");

			try {

				runOnce (
					taskLogger);

			} catch (Exception exception) {

				Logger logger =
					Logger.getLogger (
						getClass ());

				logger.error (
					generalErrorSummary (),
					exception);

				exceptionLogger.logThrowableWithSummary (
					taskLogger,
					"daemon",
					generalErrorSource (),
					generalErrorSummary (),
					exception,
					optionalAbsent (),
					GenericExceptionResolution.tryAgainLater);

			}

			// work out next delay

			delay =
				getSleepDuration ()

				.plus (
					Duration.millis (
						randomLogic.randomInteger (
							getSleepDuration ().getMillis () / 2)))

				.minus (
					Duration.millis (
						randomLogic.randomInteger (
							getSleepDuration ().getMillis () / 2)));

		}

	}

}