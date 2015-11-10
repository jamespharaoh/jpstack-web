package wbs.framework.entity.meta;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("foreign-id-field")
@PrototypeComponent ("foreignIdFieldSpec")
@ModelMetaData
public
class ForeignIdFieldSpec
	implements ModelFieldSpec {

	@DataAttribute (
		name = "field")
	String fieldName;

	@DataAttribute (
		name = "column")
	String columnName;

}
