package wbs.framework.entity.meta;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("integer-field")
@ModelMetaData
@PrototypeComponent ("integerFieldSpec")
public
class IntegerFieldSpec
	implements ModelFieldSpec {

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute
	Boolean nullable;

	@DataAttribute (
		name = "default")
	Long defaultValue;

	@DataAttribute (
		name = "minimum")
	Long minimumValue;

	@DataAttribute (
		name = "maximum")
	Long maximumValue;

	@DataAttribute (
		name = "column")
	String columnName;

}
