package wbs.platform.misc;

import static wbs.utils.time.TimeUtils.earlierThan;
import static wbs.utils.time.TimeUtils.millisToInstant;

import lombok.NonNull;

import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

public abstract
class CachedGetter <Type> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// properties

	Long reloadTimeMs = 1000l;

	// constructors

	public
	CachedGetter (
			@NonNull Long newReloadTimeMs) {

		reloadTimeMs =
			newReloadTimeMs;

	}

	public
	void setReloadTimeSecs (
			@NonNull Long secs) {

		reloadTimeMs =
			secs * 1000;

	}

	private
	Type value;

	private
	Instant lastReload =
		millisToInstant (0);

	public abstract
	Type refresh (
			TaskLogger parentTaskLogger);

	public synchronized
	Type get (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"get");

		) {

			// call refresh if necessary

			if (
				earlierThan (
					lastReload.plus (
						reloadTimeMs),
					Instant.now ())
			) {

				value =
					refresh (
						taskLogger);

				lastReload =
					Instant.now ();

			}

			// and return

			return value;

		}

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
