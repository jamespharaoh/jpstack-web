package wbs.apn.chat.settings.console;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.forms.types.FormField;
import wbs.console.module.ConsoleSpec;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("chat-user-code-field")
@PrototypeComponent ("chatUserCodeFormFieldSpec")
public
class ChatUserCodeFormFieldSpec
	implements ConsoleSpec {

	// attributes

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute
	String label;

	@DataAttribute
	Integer size = FormField.defaultSize;

	@DataAttribute
	Boolean readOnly;

	@DataAttribute
	Boolean nullable;

}
