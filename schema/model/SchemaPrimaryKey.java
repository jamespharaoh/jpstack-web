package wbs.framework.schema.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
@Data
public
class SchemaPrimaryKey {

	List<String> columns =
		new ArrayList<String> ();

	public
	SchemaPrimaryKey addColumn (
			String column) {

		columns.add (
			column);

		return this;

	}

}
