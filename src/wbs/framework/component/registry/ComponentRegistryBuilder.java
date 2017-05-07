package wbs.framework.component.registry;

import wbs.framework.component.manager.ComponentManager;
import wbs.framework.logging.TaskLogger;

public
interface ComponentRegistryBuilder {

	ComponentRegistry registerDefinition (
			TaskLogger parentTaskLogger,
			ComponentDefinition componentDefinition);

	ComponentRegistry registerUnmanagedSingleton (
			TaskLogger parentTaskLogger,
			String componentName,
			Class <?> interfaceClass,
			Object component);

	ComponentRegistry registerXmlFilename (
			TaskLogger parentTaskLogger,
			String filename);

	// request components (messy)

	ComponentRegistry addRequestComponentName (
			String name);

	// build

	ComponentManager build ();

}
