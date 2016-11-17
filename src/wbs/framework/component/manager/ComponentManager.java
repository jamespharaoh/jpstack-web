package wbs.framework.component.manager;

import java.io.Closeable;
import java.util.List;
import java.util.function.Supplier;

import javax.inject.Provider;

import com.google.common.base.Optional;

import wbs.framework.logging.TaskLogger;

public
interface ComponentManager
	extends Closeable {

	<ComponentType>
	Optional <ComponentType> getComponent (
			TaskLogger taskLogger,
			String componentName,
			Class <ComponentType> componentClass);

	<ComponentType>
	ComponentType getComponentRequired (
			TaskLogger taskLogger,
			String componentName,
			Class <ComponentType> componentClass);

	<ComponentType>
	ComponentType getComponentOrElse (
			TaskLogger taskLogger,
			String componentName,
			Class <ComponentType> componentClass,
			Supplier <ComponentType> orElse);

	<ComponentType>
	Provider <ComponentType> getComponentProviderRequired (
			TaskLogger taskLogger,
			String componentName,
			Class <ComponentType> componentClass);

	List <String> requestComponentNames ();

	@Override
	void close ();

}
