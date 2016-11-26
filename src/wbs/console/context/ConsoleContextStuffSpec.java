package wbs.console.context;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleData;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("context-stuff")
@PrototypeComponent ("consoleContextStuffSpec")
@ConsoleModuleData
public
class ConsoleContextStuffSpec {

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute
	String template;

	@DataAttribute (
		name = "field")
	String fieldName;

	@DataAttribute (
		name = "value")
	String value;

	@DataAttribute (
		name = "type")
	String type;

}
