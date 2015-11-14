package wbs.framework.entity.meta;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("type-code-field")
@PrototypeComponent ("typeCodeFieldSpec")
@ModelMetaData
public
class TypeCodeFieldSpec
	implements ModelFieldSpec {

	@DataAttribute
	String name;

	@DataAttribute (
		name = "column")
	String columnName;

}
