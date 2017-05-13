package wbs.console.formaction;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleData;
import wbs.console.module.ConsoleModuleSpec;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("context-tab-form-action-page")
@PrototypeComponent ("contextTabFormActionPageSpec")
public
class ContextTabFormActionPageSpec
	implements ConsoleModuleData {

	// tree attributes

	@DataAncestor
	ConsoleModuleSpec consoleSpec;

	// attributes

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute (
		name = "action-form")
	String actionFormContextName;

	@DataAttribute
	String helpText;

	@DataAttribute (
		required = true)
	String submitLabel;

	@DataAttribute (
		name = "helper")
	String helperName;

	@DataAttribute (
		name = "history-heading")
	String historyHeading;

	@DataAttribute (
		name = "history-form")
	String historyFormContextName;

}
