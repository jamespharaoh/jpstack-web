package wbs.applications.imchat.api;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass
public
class ImChatCustomerData {

	@DataAttribute
	String code;

	@DataAttribute
	String email;

	@DataAttribute
	Integer balance;

	@DataAttribute
	String balanceString;

}
