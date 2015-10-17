package wbs.framework.entity.meta;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("floating-point-field")
@ModelMetaData
@PrototypeComponent ("floatingPointFieldSpec")
public
class FloatingPointFieldSpec
	implements ModelFieldSpec {

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute
	Boolean nullable;

	@DataAttribute (
		value = "default")
	Boolean defaultValue;

	@DataAttribute (
		value = "column")
	String columnName;

}
