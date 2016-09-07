package wbs.framework.entity.meta.fields;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.entity.meta.model.ModelFieldSpec;
import wbs.framework.entity.meta.model.ModelMetaData;

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
		name = "default")
	Boolean defaultValue;

	@DataAttribute (
		name = "column")
	String columnName;

}
