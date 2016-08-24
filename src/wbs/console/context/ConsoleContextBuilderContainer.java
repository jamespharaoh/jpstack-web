package wbs.console.context;

import wbs.console.helper.ConsoleHelper;
import wbs.console.module.SimpleConsoleBuilderContainer;
import wbs.framework.entity.record.Record;

public
interface ConsoleContextBuilderContainer<
	ObjectType extends Record<ObjectType>
>
	extends SimpleConsoleBuilderContainer {

	String structuralName ();

	String extensionPointName ();

	String pathPrefix ();

	ConsoleHelper<ObjectType> consoleHelper ();

	String friendlyName ();

	String tabLocation ();

}
