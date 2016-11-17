package wbs.console.combo;

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
		name = "fields")
	String fieldsName;

	@DataAttribute
	String helpText;

	@DataAttribute
	String submitLabel;

	@DataAttribute (
		name = "helper")
	String helperName;

}
