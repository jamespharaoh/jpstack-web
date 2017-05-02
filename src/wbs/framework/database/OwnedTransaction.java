package wbs.framework.database;

import wbs.framework.logging.OwnedTaskLogger;

public
interface OwnedTransaction
	extends
		Transaction,
		OwnedTaskLogger {

}
