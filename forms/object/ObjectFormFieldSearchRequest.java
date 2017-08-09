package wbs.console.forms.object;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass
@ToString
public
class ObjectFormFieldSearchRequest {

	@DataAttribute
	String fieldId;

	@DataAttribute
	Long objectTypeId;

	@DataAttribute
	Long rootObjectTypeId;

	@DataAttribute
	Long rootObjectId;

	@DataAttribute
	String searchText;

}
