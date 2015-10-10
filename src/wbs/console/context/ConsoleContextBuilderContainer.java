package wbs.console.context;

import wbs.console.helper.ConsoleHelper;
import wbs.console.module.SimpleConsoleBuilderContainer;

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
