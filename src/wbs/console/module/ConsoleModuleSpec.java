package wbs.console.module;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@EqualsAndHashCode (of = "name")
@ToString (of = "name")
@DataClass ("console-module")
@PrototypeComponent ("consoleModuleSpec")
public
class ConsoleModuleSpec
	implements ConsoleModuleData {

	@DataAttribute (
		required = true)
	String name;

	@DataChildren (
		direct = true)
	List <Object> builders =
		new ArrayList<> ();

}
