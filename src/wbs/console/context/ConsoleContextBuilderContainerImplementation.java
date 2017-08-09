package wbs.console.context;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.helper.core.ConsoleHelper;
import wbs.console.module.ConsoleModuleSpec;

import wbs.framework.entity.record.Record;

@Accessors (fluent = true)
@Data
public
class ConsoleContextBuilderContainerImplementation <
	ObjectType extends Record <ObjectType>
>
	implements ConsoleContextBuilderContainer <ObjectType> {

	ConsoleModuleSpec consoleModule;

	String existingBeanNamePrefix;
	String newBeanNamePrefix;

	String structuralName;
	String extensionPointName;
	String pathPrefix;
	ConsoleHelper <ObjectType> consoleHelper;
	String friendlyName;
	String tabLocation;

}
