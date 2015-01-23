package wbs.platform.console.helper;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.platform.console.module.ConsoleModuleData;

@Accessors (fluent = true)
@Data
@DataClass ("run-post-processor")
@PrototypeComponent ("runPostProcessorSpec")
@ConsoleModuleData
public
class RunPostProcessorSpec {

	@DataAttribute
	String name;

}
