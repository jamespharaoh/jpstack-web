package wbs.clients.apn.chat.help.console;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.platform.console.module.ConsoleModuleData;
import wbs.platform.console.module.ConsoleModuleSpec;

@Accessors (fluent = true)
@Data
@EqualsAndHashCode (of = "type")
@ToString (of = "type")
@DataClass ("chat-help-template-context")
@PrototypeComponent ("chatHelpTemplateContextSpec")
@ConsoleModuleData
public
class ChatHelpTemplateContextSpec {

	// attributes

	@DataAncestor
	ConsoleModuleSpec consoleSpec;

	@DataAttribute (
		required = true)
	String type;

}
