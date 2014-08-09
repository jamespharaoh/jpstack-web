package wbs.platform.exception.console;

import javax.inject.Inject;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.platform.exception.model.ExceptionLogObjectHelper;
import wbs.platform.misc.CachedGetter;

@SingletonComponent ("numFatalExceptionsCache")
public
class NumFatalExceptionsCache
	extends CachedGetter<Integer> {

	@Inject
	ExceptionLogObjectHelper exceptionLogHelper;;

	public
	NumFatalExceptionsCache () {
		super (1000);
	}

	@Override
	public
	Integer refresh () {
		return exceptionLogHelper.countWithAlertAndFatal ();
	}

}
