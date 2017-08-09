package wbs.framework.schema.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
@Data
public
class SchemaColumn {

	String name;
	String type;
	Boolean nullable;

}
