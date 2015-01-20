package wbs.platform.console.context;

import wbs.platform.console.helper.ConsoleHelper;
import wbs.platform.console.module.SimpleConsoleBuilderContainer;

public
interface ConsoleContextBuilderContainer
	extends SimpleConsoleBuilderContainer {

	String structuralName ();

	String extensionPointName ();

	String pathPrefix ();

	ConsoleHelper<?> consoleHelper ();

	String friendlyName ();

	String tabLocation ();

}
