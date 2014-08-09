package wbs.platform.misc;

import java.util.Date;

import javax.inject.Provider;

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
	long lastReload = 0;

	public abstract
	Type refresh ();

	@Override
	public synchronized
	Type get () {

		// call refresh if necessary

		long now =
			new Date ().getTime ();

		if (lastReload + reloadTimeMs < now) {

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
