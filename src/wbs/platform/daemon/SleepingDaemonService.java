package wbs.platform.daemon;

import java.util.Random;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import wbs.platform.exception.logic.ExceptionLogic;

abstract public
class SleepingDaemonService
	extends AbstractDaemonService {

	@Inject
	ExceptionLogic exceptionLogic;

	@Inject
	Random random;

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
			random.nextInt (getDelayMs ());

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

				exceptionLogic.logThrowableWithSummary (
					"daemon",
					generalErrorSource (),
					generalErrorSummary (),
					exception,
					null,
					false);

			}

			// work out next delay

			delay =
				getDelayMs ()
					+ random.nextInt (getDelayMs () / 2)
					- random.nextInt (getDelayMs () / 2);

		}

	}

}