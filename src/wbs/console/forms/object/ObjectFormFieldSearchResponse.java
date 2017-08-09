package wbs.console.forms.object;

import java.util.List;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass
@ToString
public
class ObjectFormFieldSearchResponse {

	@DataAttribute
	String fieldId;

	@DataChildren (
		childElement = "items")
	List <ObjectFormFieldSearchResponseItem> items;

}
