package wbs.framework.entity.meta.fields;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.entity.meta.model.ModelFieldSpec;
import wbs.framework.entity.meta.model.ModelMetaData;

@Accessors (fluent = true)
@Data
@DataClass ("string-field")
@ModelMetaData
@PrototypeComponent ("stringFieldSpec")
public
class StringFieldSpec
	implements ModelFieldSpec {

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute
	Boolean nullable;

	@DataAttribute (
		name = "default")
	String defaultValue;

	@DataAttribute (
		name = "column")
	String columnName;

}
