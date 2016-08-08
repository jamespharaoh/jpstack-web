package wbs.platform.daemon;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import lombok.extern.log4j.Log4j;

import wbs.framework.utils.ThreadManager;

/**
 * AffiliateObjectHelper class for those wishing to implement DaemonService.
 */
@Log4j
public abstract
class AbstractDaemonService {

	private
	List<Thread> threads;

	protected
	AbstractDaemonService () {
	}

	@Inject
	protected
	ThreadManager threadManager;

	/**
	 * Override this if your service only need a single-thread.
	 */
	protected
	void runService () {
	}

	protected
	void init () {
	}

	protected
	void deinit () {
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

		threads.add (thread);

	}

	/**
	 * Called by startService to create worker threads. By default creates a
	 * single thread, with the name specified in the constructor (optional),
	 * which calls runService ().
	 */
	protected
	void createThreads () {

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
	@PostConstruct
	public synchronized
	void startService () {

		// if already running do nothing

		if (threads != null)
			return;

		log.info (
			stringFormat (
				"Starting %s",
				getClass ().getSimpleName ()));

		// call init

		init ();

		// ok start threads

		threads =
			new ArrayList<Thread> ();

		createThreads ();

	}

	/**
	 * Interrupts and waits for each registered thread. Will simply return if
	 * startService () has never been called or stopService () has been called
	 * since.
	 */
	@PreDestroy
	public synchronized
	void stopService () {

		// if we have never started anything, do nothing

		if (threads == null)
			return;

		log.info (
			stringFormat (
				"Stopping %s",
				getClass ().getSimpleName ()));

		// interrupt all the threads

		for (Thread thread : threads) {

			if (thread.isAlive ())
				thread.interrupt ();

		}

		// now wait for them to join us

		while (true) {

			try {

				for (Thread thread : threads) {

					if (thread.isAlive ())
						thread.join ();

				}

				break;

			} catch (InterruptedException exception) {
				continue;
			}

		}

		// call deinit

		deinit ();

		// we're done

		threads = null;

	}

}
