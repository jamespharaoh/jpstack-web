package wbs.platform.console.forms;

import wbs.platform.console.helper.ConsoleHelper;

public
interface FormFieldBuilderContext {

	Class<?> containerClass ();

	ConsoleHelper<?> consoleHelper ();

}
