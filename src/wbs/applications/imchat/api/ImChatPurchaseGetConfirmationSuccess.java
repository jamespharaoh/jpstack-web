package wbs.applications.imchat.api;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass
public
class ImChatPurchaseGetConfirmationSuccess {

	@DataAttribute
	String status = "success";

	@DataAttribute
	String sessionSecret;

	@DataAttribute
	ImChatCustomerData customer;

	@DataAttribute
	ImChatPurchaseData purchase;

}
