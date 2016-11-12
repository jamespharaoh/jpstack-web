package wbs.framework.component.manager;

import wbs.framework.logging.TaskLogger;

public
interface ComponentProvider <Type> {

	Type provide (
			TaskLogger taskLogger);

}
