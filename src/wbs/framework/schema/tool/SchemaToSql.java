package wbs.framework.schema.tool;

import java.util.List;
import java.util.Map;

import wbs.framework.schema.model.Schema;
import wbs.framework.schema.model.SchemaTable;

public
interface SchemaToSql {

	void forSchema (
			List<String> sqlStatements,
			Schema schema);

	void forSchemaTables (
			List<String> sqlStatements,
			List<SchemaTable> schemaTables);

	void forEnumTypes (
			List<String> sqlStatements,
			Map<String,List<String>> enumTypes);

}
