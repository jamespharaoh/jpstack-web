package wbs.platform.daemon;

import static wbs.utils.etc.TypeUtils.classNameSimple;

import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.NormalLifecycleTeardown;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.utils.thread.ThreadManager;

/**
 * AffiliateObjectHelper class for those wishing to implement DaemonService.
 */
public abstract
class AbstractDaemonService {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	protected
	ThreadManager threadManager;

	// state

	private
	List <Thread> threads;

	protected
	AbstractDaemonService () {
	}

	// method stubs

	protected
	boolean checkEnabled () {
		return true;
	}

	/**
	 * Override this if your service only need a single-thread.
	 */
	protected
	void runService () {
	}

	protected
	void setupService (
			@NonNull TaskLogger parentTaskLogger) {
	}

	protected
	void serviceTeardown (
			@NonNull TaskLogger parentTaskLogger) {
	}

	protected
	String getThreadName () {

		throw new UnsupportedOperationException ();

	}

	/**
	 * Should be called for each thread created so that stopService can manage
	 * them.
	 */
	protected
	void registerThread (
			Thread thread) {

		threads.add (
			thread);

	}

	/**
	 * Called by startService to create worker threads. By default creates a
	 * single thread, with the name specified in the constructor (optional),
	 * which calls runService ().
	 */
	protected
	void createThreads (
			@NonNull TaskLogger parentTaskLogger) {

		createThread (
			getThreadName (),
			new Runnable () {

				@Override
				public
				void run () {
					runService ();
				}

			});

	}

	protected
	void createThread (
			String name,
			Runnable runnable) {

		Thread thread =
			threadManager.makeThread (
				runnable,
				name);

		thread.start ();

		registerThread (
			thread);

	}

	/**
	 * Sets up some stuff and calls createThreads (). Will simply return if
	 * startService () has already been called since the last stopService ().
	 */
	@NormalLifecycleSetup
	public synchronized
	void startService (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"startService");

		) {

			// check if we are enabled

			if (! checkEnabled ()) {

				taskLogger.noticeFormat (
					"Not starting %s (disabled)",
					classNameSimple (
						getClass ()));

				return;

			}

			// if already running do nothing

			if (threads != null)
				return;

			taskLogger.noticeFormat (
				"Starting %s",
				classNameSimple (
					getClass ()));

			// call init

			setupService (
				taskLogger);

			// start threads

			threads =
				new ArrayList<> ();

			createThreads (
				taskLogger);

			// done

			taskLogger.noticeFormat (
				"Started %s",
				classNameSimple (
					getClass ()));

		}

	}

	/**
	 * Interrupts and waits for each registered thread. Will simply return if
	 * startService () has never been called or stopService () has been called
	 * since.
	 */
	@NormalLifecycleTeardown
	public synchronized
	void stopService (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"stopService");

		) {

			// if we have never started anything, do nothing

			if (threads == null)
				return;

			taskLogger.noticeFormat (
				"Stopping %s",
				getClass ().getSimpleName ());

			// interrupt all the threads

			for (Thread thread : threads) {

				if (thread.isAlive ())
					thread.interrupt ();

			}

			// now wait for them to join us

			while (true) {

				try {

					for (Thread thread : threads) {

						if (thread.isAlive ()) {
							thread.join ();
						}

					}

					break;

				} catch (InterruptedException exception) {
					continue;
				}

			}

			// call deinit

			serviceTeardown (
				taskLogger);

			// we're done

			taskLogger.noticeFormat (
				"Stopped %s",
				getClass ().getSimpleName ());

			threads = null;

		}

	}

}
