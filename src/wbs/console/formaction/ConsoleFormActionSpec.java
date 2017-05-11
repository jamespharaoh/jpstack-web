package wbs.console.formaction;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleData;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("form-action")
@PrototypeComponent ("contextFormActionSpec")
@ConsoleModuleData
public
class ConsoleFormActionSpec {

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute (
		name = "action-form")
	String actionFormContextName;

	@DataAttribute
	String helpText;

	@DataAttribute
	String submitLabel;

	@DataAttribute (
		name = "helper")
	String helperName;

	@DataAttribute
	String historyHeading;

	@DataAttribute (
		name = "history-form")
	String historyFormContextName;

}
