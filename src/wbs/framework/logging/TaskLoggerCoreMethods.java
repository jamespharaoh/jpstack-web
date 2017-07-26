package wbs.framework.logging;

public
interface TaskLoggerCoreMethods {

	Boolean debugEnabled ();

	RealTaskLogger realTaskLogger ();

	ParentTaskLogger parentTaskLogger ();

}
