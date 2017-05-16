package wbs.console.object;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.context.ConsoleContextSpec;
import wbs.console.module.ConsoleModuleSpec;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("extend-context")
@PrototypeComponent ("extendContextSpec")
public
class ExtendContextSpec
	implements ConsoleContextSpec {

	// attributes

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute (
		required = true)
	String baseName;

	@DataAncestor
	ConsoleModuleSpec consoleSpec;

	@DataAttribute (
		name = "extension-point",
		required = true)
	String extensionPointName;

	@DataAttribute
	String objectName;

	// children

	@DataChildren (
		direct = true)
	List<Object> children =
		new ArrayList<Object> ();

}
