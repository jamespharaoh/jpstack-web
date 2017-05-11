package wbs.console.forms.core;

import wbs.console.helper.core.ConsoleHelper;

public
interface FormFieldBuilderContext {

	Class <?> containerClass ();

	ConsoleHelper <?> consoleHelper ();

}
