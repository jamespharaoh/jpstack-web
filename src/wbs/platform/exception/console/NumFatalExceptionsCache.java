package wbs.platform.exception.console;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.platform.exception.model.ExceptionLogObjectHelper;
import wbs.platform.misc.CachedGetter;

@SingletonComponent ("numFatalExceptionsCache")
public
class NumFatalExceptionsCache
	extends CachedGetter <Long> {

	// singleton dependencies

	@SingletonDependency
	ExceptionLogObjectHelper exceptionLogHelper;;

	// constructors

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
