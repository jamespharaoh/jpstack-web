package wbs.framework.entity.meta.identities;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.entity.meta.model.ModelFieldSpec;
import wbs.framework.entity.meta.model.ModelMetaData;

@Accessors (fluent = true)
@Data
@DataClass ("code-field")
@PrototypeComponent ("codeFieldSpec")
@ModelMetaData
public
class CodeFieldSpec
	implements ModelFieldSpec {

	@DataAttribute
	String name;

	@DataAttribute
	String columnName;

	@DataAttribute (
		name = "cacheable")
	Boolean cacheable;

}
