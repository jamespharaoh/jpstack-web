package wbs.framework.schema.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
@Data
public
class SchemaForeignKey {

	List<String> sourceColumns =
		new ArrayList<String> ();

	String targetTable;

	public
	SchemaForeignKey addSourceColumn (
			String name) {

		sourceColumns.add (
			name);

		return this;

	}

}
