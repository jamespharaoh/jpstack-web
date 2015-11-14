package wbs.framework.entity.meta;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("enum-field")
@ModelMetaData
@PrototypeComponent ("enumFieldSpec")
public
class EnumFieldSpec
	implements ModelFieldSpec {

	@DataAttribute
	String name;

	@DataAttribute
	Boolean nullable;

	@DataAttribute (
		name = "type",
		required = true)
	String typeName;

	@DataAttribute (
		name = "default")
	String defaultValue;

	@DataAttribute (
		name = "column")
	String columnName;

}
