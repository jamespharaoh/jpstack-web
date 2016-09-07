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
@DataClass ("identity-date-field")
@ModelMetaData
@PrototypeComponent ("identityDateFieldSpec")
public
class IdentityDateFieldSpec
	implements ModelFieldSpec {

	@DataAttribute
	String name;

	@DataAttribute
	String columnName;

}
