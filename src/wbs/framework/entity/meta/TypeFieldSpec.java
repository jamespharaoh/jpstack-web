package wbs.framework.entity.meta;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("type-field")
@ModelMetaData
@PrototypeComponent ("typeFieldSpec")
public
class TypeFieldSpec
	implements ModelFieldSpec {

	@DataAttribute
	String name;

	@DataAttribute (
		name = "type")
	String typeName;

	@DataAttribute (
		name = "column")
	String columnName;

}
