package wbs.platform.object.summary;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;
import wbs.platform.console.spec.ConsoleModuleData;
import wbs.platform.console.spec.ConsoleSpec;

@Accessors (fluent = true)
@Data
@DataClass ("bean-part")
@PrototypeComponent ("objectSummaryBeanPartSpec")
@ConsoleModuleData
public
class ObjectSummaryBeanPartSpec {

	// tree attributes

	@DataAncestor
	ConsoleSpec consoleModule;

	@DataParent
	ObjectSummaryPageSpec objectSummaryPageSpec;

	@DataAttribute (
		value = "name",
		required = true)
	String beanName;

}
