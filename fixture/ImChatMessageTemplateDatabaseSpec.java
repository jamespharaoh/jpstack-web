package wbs.services.messagetemplate.fixture;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("im-chat-message-template-database")
public 
class ImChatMessageTemplateDatabaseSpec {
	
	@DataChildren (
			direct = true)
		List<MessagesSpec> messages =
			new ArrayList<MessagesSpec> ();

}
