package wbs.framework.entity.meta;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("timestamp-field")
@PrototypeComponent ("timestampFieldSpec")
@ModelMetaData
public
class TimestampFieldSpec
	implements ModelFieldSpec {

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute (
		required = true)
	ColumnType columnType;

	public static
	enum ColumnType {
		timestamp;
	}

}
