package wbs.framework.component.tools;

import wbs.framework.component.registry.ComponentRegistryBuilder;
import wbs.framework.component.scaffold.PluginSpec;
import wbs.framework.logging.TaskLogger;

public
interface ComponentPlugin {

	void registerComponents (
			TaskLogger parentTaskLogger,
			ComponentRegistryBuilder componentRegistry,
			PluginSpec plugin);

}
