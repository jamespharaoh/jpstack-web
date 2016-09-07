package wbs.platform.exception.console;

import javax.inject.Inject;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.platform.exception.model.ExceptionLogObjectHelper;
import wbs.platform.misc.CachedGetter;

@SingletonComponent ("numFatalExceptionsCache")
public
class NumFatalExceptionsCache
	extends CachedGetter <Long> {

	@Inject
	ExceptionLogObjectHelper exceptionLogHelper;;

	public
	NumFatalExceptionsCache () {
		super (1000);
	}

	@Override
	public
	Long refresh () {
		return exceptionLogHelper.countWithAlertAndFatal ();
	}

}
