package wbs.framework.database;

import java.sql.Connection;

public
interface WbsConnection
	extends Connection {

	long serverProcessId ();

}
