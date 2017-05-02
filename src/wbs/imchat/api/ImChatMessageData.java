package wbs.imchat.api;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass
public
class ImChatMessageData {

	@DataAttribute
	Long index;

	@DataAttribute
	String sender;

	@DataAttribute
	String messageText;

	@DataAttribute
	Long timestamp;

	@DataAttribute
	Long charge;

	@DataAttribute
	String chargeString;

}
