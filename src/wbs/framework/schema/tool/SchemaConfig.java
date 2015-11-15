package wbs.framework.schema.tool;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.sql.SqlLogic;
import wbs.framework.sql.SqlLogicImplementation;

@SingletonComponent ("schemaConfig")
public
class SchemaConfig {

	@SingletonComponent ("sqlLogic")
	public
	SqlLogic sqlLogic () {
		return new SqlLogicImplementation ();
	}

}
