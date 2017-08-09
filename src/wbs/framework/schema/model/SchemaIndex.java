package wbs.framework.schema.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
@Data
public
class SchemaIndex {

	String name;

	List<String> columns =
		new ArrayList<String> ();

	Boolean unique;

	public
	SchemaIndex addColumn (
			String column) {

		columns.add (
			column);

		return this;

	}

}
