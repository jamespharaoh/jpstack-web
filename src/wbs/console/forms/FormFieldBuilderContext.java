package wbs.console.forms;

import wbs.console.helper.core.ConsoleHelper;

public
interface FormFieldBuilderContext {

	Class<?> containerClass ();

	ConsoleHelper<?> consoleHelper ();

}
