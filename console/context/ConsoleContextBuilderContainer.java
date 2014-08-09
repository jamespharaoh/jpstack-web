package wbs.platform.console.context;

import wbs.platform.console.helper.ConsoleHelper;
import wbs.platform.console.spec.ConsoleSimpleBuilderContainer;

public
interface ConsoleContextBuilderContainer
	extends ConsoleSimpleBuilderContainer {

	String structuralName ();

	String extensionPointName ();

	String pathPrefix ();

	ConsoleHelper<?> consoleHelper ();

	String friendlyName ();

	String tabLocation ();

}
