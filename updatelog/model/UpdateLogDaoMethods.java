package wbs.platform.updatelog.model;

public
interface UpdateLogDaoMethods {

	UpdateLogRec findByTableAndRef (
			String table,
			int ref);

}