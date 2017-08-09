package wbs.framework.schema.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
@Data
public
class Schema {

	Map<String,List<String>> enumTypes =
		new HashMap<String,List<String>> ();

	Map<String,SchemaTable> tables =
		new HashMap<String,SchemaTable> ();

	public
	Schema addEnumType (
			String name,
			List<String> values) {

		enumTypes.put (
			name,
			values);

		return this;

	}

	public
	Schema addTable (
			SchemaTable schemaTable) {

		tables.put (
			schemaTable.name (),
			schemaTable);

		return this;

	}

}
