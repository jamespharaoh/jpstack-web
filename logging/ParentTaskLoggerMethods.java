package wbs.framework.logging;

public
interface ParentTaskLoggerMethods
	extends TaskLoggerCoreMethods {

	Long nesting ();

	void increaseErrorCount ();

	void increaseWarningCount ();

	void increaseNoticeCount ();

	void increaseLogicCount ();

	void increaseDebugCount ();

	void writeFirstError ();

	void addChild (
			OwnedTaskLogger child);

}
