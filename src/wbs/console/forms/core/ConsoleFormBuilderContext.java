package wbs.console.forms.core;

import wbs.console.helper.core.ConsoleHelper;

public
interface ConsoleFormBuilderContext {

	Class <?> containerClass ();

	ConsoleHelper <?> consoleHelper ();

}
