package wbs.framework.entity.meta;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("reference-field")
@ModelMetaData
@PrototypeComponent ("referenceFieldSpec")
public
class ReferenceFieldSpec
	implements ModelFieldSpec {

	@DataAttribute (
		value = "type",
		required = true)
	String typeName;

}
