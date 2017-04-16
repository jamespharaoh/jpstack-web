package wbs.platform.exception.console;

import lombok.NonNull;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;

import wbs.platform.exception.model.ExceptionLogObjectHelper;
import wbs.platform.misc.CachedGetter;

@SingletonComponent ("numExceptionsCache")
public
class NumExceptionsCache
	extends CachedGetter <Long> {

	// singleton dependencies

	@SingletonDependency
	ExceptionLogObjectHelper exceptionLogHelper;

	// constructors

	public
	NumExceptionsCache () {
		super (1000l);
	}

	@Override
	public
	Long refresh (
			@NonNull TaskLogger parentTaskLogger) {

		return exceptionLogHelper.countWithAlert ();

	}

}
