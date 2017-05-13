package wbs.console.helper.spec;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleData;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("priv-key")
@PrototypeComponent ("privKeySpec")
public
class PrivKeySpec
	implements ConsoleModuleData {

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute (
		name = "priv",
		required = true)
	String privCode;

}
