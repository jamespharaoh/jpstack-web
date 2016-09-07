package wbs.framework.entity.meta.identities;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.entity.meta.model.ModelFieldSpec;
import wbs.framework.entity.meta.model.ModelMetaData;

@Accessors (fluent = true)
@Data
@DataClass ("slave-field")
@ModelMetaData
@PrototypeComponent ("slaveFieldSpec")
public
class SlaveFieldSpec
	implements ModelFieldSpec {

	@DataAttribute
	String name;

	@DataAttribute (
		name = "type",
		required = true)
	String typeName;

}
