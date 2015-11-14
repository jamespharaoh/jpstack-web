package wbs.platform.object.summary;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleData;
import wbs.console.module.ConsoleModuleSpec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;

@Accessors (fluent = true)
@Data
@DataClass ("bean-part")
@PrototypeComponent ("objectSummaryBeanPartSpec")
@ConsoleModuleData
public
class ObjectSummaryBeanPartSpec {

	// tree attributes

	@DataAncestor
	ConsoleModuleSpec consoleModule;

	@DataParent
	ObjectSummaryPageSpec objectSummaryPageSpec;

	@DataAttribute (
		name = "name",
		required = true)
	String beanName;

}
