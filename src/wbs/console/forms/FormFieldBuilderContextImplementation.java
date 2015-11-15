package wbs.console.forms;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.helper.ConsoleHelper;

@Accessors (fluent = true)
public
class FormFieldBuilderContextImplementation
	implements FormFieldBuilderContext {

	@Getter @Setter
	Class<?> containerClass;

	@Getter @Setter
	ConsoleHelper<?> consoleHelper;

}
