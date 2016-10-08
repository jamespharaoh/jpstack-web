package wbs.console.module;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
@Data
public
class SimpleConsoleBuilderContainerImplementation
	implements SimpleConsoleBuilderContainer {

	TaskLogger taskLogger;

	String newBeanNamePrefix;
	String existingBeanNamePrefix;

}
