package wbs.framework.utils;

import static wbs.framework.utils.etc.EnumUtils.enumEqualSafe;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import javax.inject.Inject;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;

@Accessors (fluent = true)
@Log4j
@PrototypeComponent ("threadManagerImplementation")
public
class ThreadManagerImplementation
	implements ThreadManager {

	// dependencies

	@Inject
	ExceptionLogger exceptionLogger;

	// properties

	@Getter @Setter
	String exceptionTypeCode;

	// implementation

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
				@NonNull Throwable throwable,
				@NonNull GenericExceptionResolution resolution) {

			if (
				enumEqualSafe (
					resolution,
					GenericExceptionResolution.fatalError)
			) {

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
					exceptionTypeCode,
					stringFormat (
						"Thread %s",
						getName ()),
					throwable,
					Optional.absent (),
					resolution);

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
					GenericExceptionResolution.fatalError);

			}

		}

	}

	/**
	 * Create and return a managed thread, using the given Runnable as the
	 * target.
	 */
	@Override
	public
	Thread makeThread (
			@NonNull Runnable target) {

		return new ManagedThread (
			target);

	}

	/**
	 * Create, start and return a managed thread, using the given Runnable as
	 * the target.
	 */
	@Override
	public
	Thread startThread (
			@NonNull Runnable target) {

		Thread thread =
			makeThread (
				target);

		thread.start ();

		return thread;

	}

	/**
	 * Create and return a managed thread, using the given Runnable as the
	 * target, and with the given name.
	 */
	@Override
	public
	Thread makeThread (
			@NonNull Runnable target,
			@NonNull String name) {

		return new ManagedThread (
			target,
			name);

	}

	/**
	 * Create, start and return a managed thread, using the given Runnable as
	 * the target, and with the given name.
	 */
	@Override
	public
	Thread startThread (
			@NonNull Runnable target,
			@NonNull String name) {

		Thread thread =
			makeThread (
				target,
				name);

		thread.start ();

		return thread;

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
