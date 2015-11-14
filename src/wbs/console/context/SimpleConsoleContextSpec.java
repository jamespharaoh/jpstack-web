package wbs.console.context;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleData;
import wbs.console.module.ConsoleModuleSpec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@EqualsAndHashCode (of = "name")
@ToString (of = "name")
@DataClass ("simple-context")
@PrototypeComponent ("simpleConsoleContextSpec")
@ConsoleModuleData
public
class SimpleConsoleContextSpec {

	// tree attributes

	@DataAncestor
	ConsoleModuleSpec consoleModule;

	// attributes

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute
	String typeName;

	@DataAttribute
	String title;

	// children

	@DataChildren (
		direct = true)
	List<Object> children =
		new ArrayList<Object> ();

}
