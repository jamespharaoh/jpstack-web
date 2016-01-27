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
	String requiredLabel;

	@DataAttribute
	String dataType;

	@DataAttribute
	Long minimumAge;

	@DataAttribute
	String value;

	@DataAttribute
	String requiredErrorTitle;

	@DataAttribute
	String requiredErrorMessage;

	@DataAttribute
	String invalidErrorTitle;

	@DataAttribute
	String invalidErrorMessage;

	@DataAttribute
	String ageErrorTitle;

	@DataAttribute
	String ageErrorMessage;

}
