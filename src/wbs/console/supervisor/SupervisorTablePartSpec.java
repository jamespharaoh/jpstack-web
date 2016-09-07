package wbs.console.supervisor;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleData;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;

@Accessors (fluent = true)
@Data
@DataClass ("table")
@PrototypeComponent ("supervisorTablePartSpec")
@ConsoleModuleData
public
class SupervisorTablePartSpec {

	@DataParent
	SupervisorConfigSpec supervisorConfig;

	@DataChildren (direct = true)
	List<Object> builders =
		new ArrayList<Object> ();

}
