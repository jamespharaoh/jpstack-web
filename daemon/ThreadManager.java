package wbs.platform.daemon;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.concurrent.ThreadFactory;

import javax.inject.Inject;

import lombok.extern.log4j.Log4j;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.exception.ExceptionLogger;

import com.google.common.base.Optional;

@Log4j
@SingletonComponent ("threadManager")
public
class ThreadManager
	implements ThreadFactory {

	public
	ThreadManager () {
	}

	@Inject
	ExceptionLogger exceptionLogger;

	/**
	 * Thread class which provides the desired functionality.
	 */
	class ManagedThread
		extends Thread {

		Runnable target;

		ManagedThread (
				Runnable target) {

			super ();

			if (target == null)
				throw new NullPointerException ();

			this.target =
				target;

		}

		ManagedThread (
				Runnable target,
				String name) {

			this (target);

			if (name != null)
				setName (name);

		}

		void logThrowable (
				Throwable throwable,
				boolean fatal) {

			if (fatal) {

				log.fatal (
					stringFormat (
						"Unhandled fatal exception in thread %s",
						getName ()),
					throwable);

			} else {

				log.error (
					stringFormat (
						"Unhandled exception in thread %s",
						getName ()),
					throwable);

			}

			throwable.printStackTrace ();

			try {

				exceptionLogger.logThrowable (
					"daemon",
					stringFormat (
						"Daemon thread %s",
						getName ()),
					throwable,
					Optional.<Integer>absent (),
					fatal);

			} catch (Throwable exception) {

				log.fatal (
					stringFormat (
						"Error logging exception in %s",
						getName ()),
					exception);

			}

		}

		@Override
		public
		void run () {

			try {

				// start the thread

				target.run ();

			} catch (Throwable throwable) {

				logThrowable (
					throwable,
					true);

			}

		}

	}

	/**
	 * Create and return a managed thread, using the given Runnable as the
	 * target.
	 */
	public
	Thread makeThread (
			Runnable target) {

		return new ManagedThread (
			target);

	}

	/**
	 * Create and return a managed thread, using the given Runnable as the
	 * target, and with the given name.
	 */
	public
	Thread makeThread (
			Runnable target,
			String name) {

		return new ManagedThread (
			target,
			name);

	}

	// thread factory

	@Override
	public
	Thread newThread (
			Runnable runnable) {

		return makeThread (
			runnable);

	}

}
