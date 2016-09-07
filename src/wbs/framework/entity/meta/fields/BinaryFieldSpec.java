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
@DataClass ("binary-field")
@ModelMetaData
@PrototypeComponent ("binaryFieldSpec")
public
class BinaryFieldSpec
	implements ModelFieldSpec {

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute
	Boolean nullable;

	@DataAttribute (
		name = "column")
	String columnName;

}
