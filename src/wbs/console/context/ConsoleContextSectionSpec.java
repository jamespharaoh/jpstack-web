package wbs.console.context;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
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
@DataClass ("section")
@PrototypeComponent ("consoleContextSectionSpec")
@ConsoleModuleData
public
class ConsoleContextSectionSpec {

	// tree attributes

	@DataAncestor
	ConsoleModuleSpec consoleSpec;

	// attributes

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute
	String aliasOf;

	@DataAttribute
	String label;

	// children

	@DataChildren (
		direct = true)
	List<Object> children =
		new ArrayList<Object> ();

}
