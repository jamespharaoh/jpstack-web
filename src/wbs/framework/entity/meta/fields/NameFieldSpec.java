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
@DataClass ("name-field")
@PrototypeComponent ("nameFieldSpec")
@ModelMetaData
public
class NameFieldSpec
	implements ModelFieldSpec {

	@DataAttribute
	String name;

	@DataAttribute
	String columnName;

}
