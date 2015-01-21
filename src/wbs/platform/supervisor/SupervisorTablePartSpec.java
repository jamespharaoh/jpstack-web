package wbs.platform.supervisor;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;
import wbs.platform.console.module.ConsoleModuleData;

@Accessors (fluent = true)
@Data
@DataClass ("table")
@PrototypeComponent ("supervisorTablePartSpec")
@ConsoleModuleData
public
class SupervisorTablePartSpec {

	@DataParent
	SupervisorPageSpec supervisorPageSpec;

	@DataChildren (direct = true)
	List<Object> builders =
		new ArrayList<Object> ();

}
