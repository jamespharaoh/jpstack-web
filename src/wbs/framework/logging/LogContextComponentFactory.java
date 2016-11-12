package wbs.framework.logging;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.tools.ComponentFactory;

@Accessors (fluent = true)
public
class LogContextComponentFactory
	implements ComponentFactory {

	// properties

	@Getter @Setter
	Class <?> componentClass;

	// public implementation

	@Override
	public
	Object makeComponent (
			@NonNull TaskLogger parentTaskLogger) {

		return DefaultLogContext.forClass (
			componentClass);

	}

}
