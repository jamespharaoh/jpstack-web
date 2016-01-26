package wbs.applications.imchat.api;

import lombok.Data;
import lombok.experimental.Accessors;

import org.json.simple.JSONObject;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass
public 
class ImChatEventItemRequest {

	@DataAttribute
	Long index;

	@DataAttribute
	Long timestamp;

	@DataAttribute
	String sessionSecret;

	@DataAttribute
	String type;

	@DataAttribute
	JSONObject payload;

}
