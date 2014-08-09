package wbs.platform.supervisor;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.platform.console.spec.ConsoleModuleData;

@Accessors (fluent = true)
@Data
@DataClass ("supervisor-page")
@PrototypeComponent ("supervisorPageSpec")
@ConsoleModuleData
public
class SupervisorPageSpec {

	@DataAttribute
	String name;

	@DataAttribute
	String tabName;

	@DataAttribute
	String tabLabel;

	@DataAttribute
	String fileName;

	@DataAttribute
	String responderName;

	@DataAttribute
	String title;

	@DataChildren (
		direct = true)
	List<Object> builders =
		new ArrayList<Object> ();

}
