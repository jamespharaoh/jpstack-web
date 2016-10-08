package wbs.framework.component.tools;

import wbs.framework.logging.TaskLogger;

public
interface ComponentFactory {

	Object makeComponent (
			TaskLogger taskLogger);

}
