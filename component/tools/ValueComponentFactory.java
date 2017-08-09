package wbs.framework.component.tools;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
public
class ValueComponentFactory <ComponentType>
	implements ComponentFactory <ComponentType> {

	// properties

	@Getter @Setter
	ComponentType component;

	// public implementation

	@Override
	public
	ComponentType makeComponent (
			@NonNull TaskLogger parentTaskLogger) {

		return component;

	}

}
