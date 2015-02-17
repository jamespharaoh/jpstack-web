package wbs.platform.supervisor;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.platform.console.module.ConsoleModuleData;

@Accessors (fluent = true)
@Data
@DataClass ("supervisor-config")
@PrototypeComponent ("supervisorConfigSpec")
@ConsoleModuleData
public
class SupervisorConfigSpec {

	@DataAttribute (
		required = true)
	String name;

	@DataChildren (
		direct = true)
	List<Object> builders =
		new ArrayList<Object> ();

}
