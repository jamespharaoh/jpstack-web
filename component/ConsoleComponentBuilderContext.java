package wbs.console.component;

import wbs.console.module.ConsoleModuleSpec;

public
interface ConsoleComponentBuilderContext {

	ConsoleModuleSpec consoleModule ();

	String structuralName ();

	String pathPrefix ();

	String friendlyName ();

	String existingComponentNamePrefix ();
	String newComponentNamePrefix ();

	String objectType ();

}
