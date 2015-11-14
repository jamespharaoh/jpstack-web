package wbs.applications.imchat.api;

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
class ImChatDetailsUpdateRequest {

	@DataAttribute
	String sessionSecret;

	@DataChildren
	Map<String,String> details;

}
