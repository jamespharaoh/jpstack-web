package wbs.framework.builder;

import wbs.framework.logging.TaskLogger;

public
interface BuilderComponent {

	void build (
			TaskLogger taskLogger,
			Builder builder);

}
