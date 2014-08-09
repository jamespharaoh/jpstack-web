package wbs.framework.activitymanager;

public
interface ActiveTask {

	void success ();

	void close ();

	<ExceptionType extends Throwable>
	ExceptionType fail (
			ExceptionType exception);

}
