package wbs.apn.chat.help.console;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleSpec;
import wbs.console.module.ConsoleSpec;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@EqualsAndHashCode (of = "type")
@ToString (of = "type")
@DataClass ("chat-help-template-context")
@PrototypeComponent ("chatHelpTemplateContextSpec")
public
class ChatHelpTemplateContextSpec
	implements ConsoleSpec {

	// attributes

	@DataAncestor
	ConsoleModuleSpec consoleSpec;

	@DataAttribute (
		required = true)
	String type;

}
