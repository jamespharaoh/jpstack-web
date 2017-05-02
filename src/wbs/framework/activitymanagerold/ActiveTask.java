package wbs.framework.activitymanagerold;

public
interface ActiveTask
	extends AutoCloseable {

	void success ();

	@Override
	void close ();

	<ExceptionType extends Throwable>
	ExceptionType fail (
			ExceptionType exception);

	ActiveTask put (
			String key,
			String value);

}
