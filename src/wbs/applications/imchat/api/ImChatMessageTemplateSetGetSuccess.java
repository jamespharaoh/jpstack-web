package wbs.applications.imchat.api;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass
public
class ImChatMessageTemplateSetGetSuccess {

	@DataAttribute
	String status = "success";

	@DataAttribute
	List<ImChatMessageTemplateData> messages =
		new ArrayList<ImChatMessageTemplateData> ();

}
