package wbs.console.supervisor;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleSpec;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("supervisor-config")
@PrototypeComponent ("supervisorConfigSpec")
public
class SupervisorConfigSpec
	implements ConsoleSpec {

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute (
		required = true)
	String label;

	@DataAttribute
	Long offsetHours;

	@DataAttribute (
		name = "template")
	String templateName;

	@DataChildren (
		direct = true)
	List <Object> builders =
		new ArrayList<> ();

}
