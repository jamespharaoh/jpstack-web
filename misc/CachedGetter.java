package wbs.platform.misc;

import static wbs.framework.utils.etc.Misc.earlierThan;

import javax.inject.Provider;

import org.joda.time.Instant;

public abstract
class CachedGetter<Type>
	implements Provider<Type> {

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

		Instant now =
			Instant.now ();

		// call refresh if necessary

		if (
			earlierThan (
				lastReload.plus (
					reloadTimeMs),
				now)
		) {

			value =
				refresh ();

			lastReload =
				now;

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
