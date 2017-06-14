package wbs.imchat.api;

import java.util.Map;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass
public
class ImChatCustomerCreateRequest {

	@DataAttribute
	String email;

	@DataAttribute
	String password;

	@DataAttribute
	String userAgent;

	@DataAttribute
	String messagesCode;

	@DataChildren
	Map <String, String> details;

}
