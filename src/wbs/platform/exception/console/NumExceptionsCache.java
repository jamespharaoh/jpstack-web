package wbs.platform.exception.console;

import javax.inject.Inject;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.platform.exception.model.ExceptionLogObjectHelper;
import wbs.platform.misc.CachedGetter;

@SingletonComponent ("numExceptionsCache")
public
class NumExceptionsCache
	extends CachedGetter<Integer> {

	@Inject
	ExceptionLogObjectHelper exceptionLogHelper;

	public
	NumExceptionsCache () {
		super (1000);
	}

	@Override
	public
	Integer refresh () {
		return exceptionLogHelper.countWithAlert ();
	}

}
