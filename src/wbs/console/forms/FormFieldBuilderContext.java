package wbs.console.forms;

import wbs.console.helper.ConsoleHelper;

public
interface FormFieldBuilderContext {

	Class<?> containerClass ();

	ConsoleHelper<?> consoleHelper ();

}
