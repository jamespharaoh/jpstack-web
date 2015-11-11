package wbs.applications.imchat.api;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass
public
class ImChatCustomerDetailData {

	@DataAttribute
	String code;

	@DataAttribute
	String label;

	@DataAttribute
	String help;

	@DataAttribute
	Boolean required;

	@DataAttribute
	String dataType;

	@DataAttribute
	Integer minimumAge;

	@DataAttribute
	Object value;

}
