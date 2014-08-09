package wbs.framework.utils.etc;

import lombok.extern.log4j.Log4j;


/**
 * Allows for easy retry of an operation.
 *
 * Use pattern:
 *
 * Tries tries = new Tries ();
 * while (tries.next ()) {
 *     try {
 *         performAction ();
 *         tries.done ();
 *     } catch (Exception exception) {
 *         tries.error (exception);
 *     }
 * }
 */
@Log4j
public
class Tries {

	public final static
	int
		defaultDelay = 100,
		defaultMax = 10;

	int max;
	int count = 0;
	int delay;

	boolean done = false;
	boolean ready = true;

	public
	Tries (
			int max,
			int delay) {

		this.max = max;
		this.delay = delay;

	}

	public
	Tries (
			int max) {

		this (
			max,
			defaultDelay);

	}

	public
	Tries () {

		this (
			defaultMax,
			defaultDelay);

	}

	public
	boolean next () {

		if (! ready) {

			throw new RuntimeException (
				"Not ready, must call done or error once only");

		}

		if (done)
			return false;

		if (count >= max)
			return false;

		if (count > 0) {

			try {
				Thread.sleep (delay);
			} catch (InterruptedException e) {
				Thread.currentThread ().interrupt ();
			}

		}

		count ++;

		ready = false;

		return true;

	}

	public
	void done () {

		if (ready) {

			throw new RuntimeException (
				"Already ready, must call done or error once only");

		}

		ready = true;
		done = true;
	}

	public
	void error (
			RuntimeException exception) {

		if (ready) {

			throw new RuntimeException (
				"Already ready, must call done or error once only");

		}

		ready =
			true;

		if (count >= max) {

			throw new RuntimeException (
				exception);

		}

	}

	@Override
	protected
	void finalize ()
			throws Throwable {

		try {

			if (! done) {

				log.warn (
					"Finalize called in unfinished state");

			}

		} finally {

			super.finalize ();
		}

	}

}