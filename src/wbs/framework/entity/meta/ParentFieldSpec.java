package wbs.framework.entity.meta;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("parent-field")
@ModelMetaData
@PrototypeComponent ("parentFieldSpec")
public
class ParentFieldSpec
	implements ModelFieldSpec {

	@DataAttribute
	String name;

	@DataAttribute (
		value = "type",
		required = true)
	String typeName;

	@DataAttribute (
		value = "column")
	String columnName;

}
