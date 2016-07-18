package wbs.framework.dbpool;

import java.sql.Connection;

public
interface WbsConnection
	extends Connection {

	long serverProcessId ();

}
