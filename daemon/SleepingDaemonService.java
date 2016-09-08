package wbs.platform.daemon;

import com.google.common.base.Optional;

import org.apache.log4j.Logger;
import org.joda.time.Duration;

import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.utils.RandomLogic;

public abstract
class SleepingDaemonService
	extends AbstractDaemonService {

	// singleton dependencies

	@SingletonDependency
	ExceptionLogger exceptionLogger;

	@SingletonDependency
	RandomLogic randomLogic;

	// hooks to override

	abstract protected
	Duration getSleepDuration ();

	abstract protected
	void runOnce ();

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

			try {

				runOnce ();

			} catch (Exception exception) {

				Logger logger =
					Logger.getLogger (
						getClass ());

				logger.error (
					generalErrorSummary (),
					exception);

				exceptionLogger.logThrowableWithSummary (
					"daemon",
					generalErrorSource (),
					generalErrorSummary (),
					exception,
					Optional.absent (),
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