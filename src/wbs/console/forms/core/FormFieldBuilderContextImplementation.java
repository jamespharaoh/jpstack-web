package wbs.console.forms.core;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.helper.core.ConsoleHelper;

@Accessors (fluent = true)
public
class FormFieldBuilderContextImplementation
	implements ConsoleFormBuilderContext {

	@Getter @Setter
	Class <?> containerClass;

	@Getter @Setter
	ConsoleHelper <?> consoleHelper;

}
