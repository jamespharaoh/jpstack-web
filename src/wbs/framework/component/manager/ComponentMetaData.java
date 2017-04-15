package wbs.framework.component.manager;

import com.google.common.base.Optional;

import wbs.framework.component.manager.ComponentManagerImplementation.ComponentState;
import wbs.framework.component.registry.ComponentDefinition;

public
interface ComponentMetaData {

	ComponentDefinition definition ();

	Optional <Object> component ();

	ComponentState state ();

}
