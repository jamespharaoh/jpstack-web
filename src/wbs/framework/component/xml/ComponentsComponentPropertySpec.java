package wbs.framework.component.xml;

import wbs.framework.component.registry.ComponentDefinition;
import wbs.framework.logging.TaskLogger;

public
interface ComponentsComponentPropertySpec {

	void register (
			TaskLogger parentTaskLogger,
			ComponentDefinition beanDefinition);

}
