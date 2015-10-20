package wbs.platform.misc;

import static wbs.framework.utils.etc.Misc.earlierThan;

import javax.inject.Inject;
import javax.inject.Provider;

import org.joda.time.Instant;

import wbs.framework.database.Database;
import wbs.framework.database.Transaction;

public abstract
class CachedGetter<Type>
	implements Provider<Type> {

	@Inject
	Database database;

	long reloadTimeMs = 1000;

	public
	CachedGetter () {
	}

	public
	CachedGetter (
			long newReloadTimeMs) {

		reloadTimeMs =
			newReloadTimeMs;

	}

	public
	void setReloadTimeSecs (
			int secs) {

		reloadTimeMs =
			secs * 1000;

	}

	private
	Type value;

	private
	Instant lastReload =
		new Instant (0);

	public abstract
	Type refresh ();

	@Override
	public synchronized
	Type get () {

		Transaction transaction =
			database.currentTransaction ();

		// call refresh if necessary

		if (
			earlierThan (
				lastReload.plus (
					reloadTimeMs),
				transaction.now ())
		) {

			value =
				refresh ();

			lastReload =
				transaction.now ();

		}

		// and return

		return value;

	}

	public
	long getReloadTimeMs () {

		return reloadTimeMs;

	}

	public
	void setReloadTimeMs (
			long reloadTimeMs) {

		this.reloadTimeMs =
			reloadTimeMs;

	}

}
