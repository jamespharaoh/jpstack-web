package wbs.platform.console.forms;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.platform.console.helper.ConsoleHelper;

@Accessors (fluent = true)
public
class FormFieldBuilderContextImpl
	implements FormFieldBuilderContext {

	@Getter @Setter
	Class<?> containerClass;

	@Getter @Setter
	ConsoleHelper<?> consoleHelper;

}
