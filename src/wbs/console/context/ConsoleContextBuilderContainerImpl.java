package wbs.console.context;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.helper.ConsoleHelper;

@Accessors (fluent = true)
@Data
public
class ConsoleContextBuilderContainerImpl
	implements ConsoleContextBuilderContainer {

	String existingBeanNamePrefix;
	String newBeanNamePrefix;

	String structuralName;
	String extensionPointName;
	String pathPrefix;
	ConsoleHelper<?> consoleHelper;
	String friendlyName;
	String tabLocation;

}
