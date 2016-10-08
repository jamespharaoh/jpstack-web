package wbs.console.context;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.helper.ConsoleHelper;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
@Data
public
class ConsoleContextBuilderContainerImplementation <
	ObjectType extends Record <ObjectType>
>
	implements ConsoleContextBuilderContainer <ObjectType> {

	TaskLogger taskLogger;

	String existingBeanNamePrefix;
	String newBeanNamePrefix;

	String structuralName;
	String extensionPointName;
	String pathPrefix;
	ConsoleHelper<ObjectType> consoleHelper;
	String friendlyName;
	String tabLocation;

}
