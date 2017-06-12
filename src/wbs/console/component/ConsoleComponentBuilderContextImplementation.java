package wbs.console.component;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleSpec;

@Accessors (fluent = true)
@Data
public
class ConsoleComponentBuilderContextImplementation
	implements ConsoleComponentBuilderContext {

	ConsoleModuleSpec consoleModule;

	String pathPrefix;

	String friendlyName;

	String existingComponentNamePrefix;
	String newComponentNamePrefix;

	String objectType;

}
