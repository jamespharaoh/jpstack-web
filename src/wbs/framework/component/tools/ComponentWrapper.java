package wbs.framework.component.tools;

import wbs.framework.component.registry.ComponentDefinition;
import wbs.framework.component.registry.ComponentRegistryBuilder;
import wbs.framework.logging.TaskLogger;

public
interface ComponentWrapper <Component> {

	Class <Component> componentClass ();

	void wrapComponent (
			TaskLogger parentTaskLogger,
			ComponentRegistryBuilder componentRegistry,
			ComponentDefinition componentDefinition);

}
