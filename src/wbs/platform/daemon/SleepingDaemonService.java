package wbs.platform.daemon;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import wbs.framework.exception.ExceptionLogger;
import wbs.framework.utils.RandomLogic;

import com.google.common.base.Optional;

abstract public
class SleepingDaemonService
	extends AbstractDaemonService {

	// dependencies

	@Inject
	ExceptionLogger exceptionLogger;

	@Inject
	RandomLogic randomLogic;

	// hooks to override

	abstract protected
	int getDelayMs ();

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

		int delay =
			randomLogic.randomInteger (
				getDelayMs ());

		while (true) {

			// delay

			try {
				Thread.sleep (delay);
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
					Optional.<Integer>absent (),
					false);

			}

			// work out next delay

			delay =
				+ getDelayMs ()
				+ randomLogic.randomInteger (
					getDelayMs () / 2)
				- randomLogic.randomInteger (
					getDelayMs () / 2);

		}

	}

}