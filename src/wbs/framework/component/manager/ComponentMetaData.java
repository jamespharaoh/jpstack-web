package wbs.framework.component.manager;

import com.google.common.base.Optional;

import wbs.framework.component.registry.ComponentDefinition;

public
interface ComponentMetaData {

	String name ();

	ComponentDefinition definition ();

	Optional <Object> component ();

	ComponentState state ();

}
