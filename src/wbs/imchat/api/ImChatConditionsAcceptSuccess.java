package wbs.imchat.api;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass
public
class ImChatConditionsAcceptSuccess {

	@DataAttribute
	String status = "success";

	@DataAttribute
	ImChatCustomerData customer;

}
