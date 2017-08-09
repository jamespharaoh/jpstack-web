package wbs.framework.database;

import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

public
interface OwnedTransaction
	extends
		CloseableTransaction,
		OwnedTaskLogger {

	void commit (
			TaskLogger parentTaskLogger);

	void closeTransaction ();

}
