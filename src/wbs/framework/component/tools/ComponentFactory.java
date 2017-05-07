package wbs.framework.component.tools;

import wbs.framework.logging.TaskLogger;

public
interface ComponentFactory <ComponentType> {

	ComponentType makeComponent (
			TaskLogger parentTaskLogger);

}
