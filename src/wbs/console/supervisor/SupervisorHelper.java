package wbs.console.supervisor;

import java.util.List;

import wbs.framework.database.Transaction;

public
interface SupervisorHelper {

	List <String> getSupervisorConfigNames (
			Transaction parentTransaction);

}
