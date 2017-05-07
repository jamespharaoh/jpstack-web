package wbs.framework.logging;

import static wbs.utils.etc.TypeUtils.classNameFull;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.tools.ComponentFactory;

@Accessors (fluent = true)
public
class LogContextComponentFactory
	implements ComponentFactory <LogContext> {

	// properties

	@Getter @Setter
	LoggingLogic loggingLogic;

	@Getter @Setter
	Class <?> componentClass;

	// public implementation

	@Override
	public
	LogContext makeComponent (
			@NonNull TaskLogger parentTaskLogger) {

		return loggingLogic.findOrCreateLogContext (
			classNameFull (
				componentClass));

	}

}
