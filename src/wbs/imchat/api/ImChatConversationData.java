package wbs.imchat.api;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass
public
class ImChatConversationData {

	@DataAttribute
	Long index;

	@DataAttribute
	ImChatProfileData profile;

	@DataAttribute
	Boolean replyPending;

}
