package wbs.framework.builder;

import wbs.framework.logging.TaskLogger;

public
interface BuilderComponent {

	void build (
			TaskLogger parentTaskLogger,
			Builder <TaskLogger> builder);

}
