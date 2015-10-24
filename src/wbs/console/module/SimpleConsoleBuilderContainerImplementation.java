package wbs.console.module;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
@Data
public
class SimpleConsoleBuilderContainerImplementation
	implements SimpleConsoleBuilderContainer {

	String newBeanNamePrefix;
	String existingBeanNamePrefix;

}
