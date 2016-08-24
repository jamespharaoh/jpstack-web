package wbs.framework.application.context;

import java.io.Closeable;
import java.util.List;
import java.util.function.Supplier;

import javax.inject.Provider;

import com.google.common.base.Optional;

public
interface ApplicationContext
	extends Closeable {

	<ComponentType>
	Optional <ComponentType> getComponent (
			String componentName,
			Class <ComponentType> componentClass);

	<ComponentType>
	ComponentType getComponentRequired (
			String componentName,
			Class <ComponentType> componentClass);

	<ComponentType>
	ComponentType getComponentOrElse (
			String componentName,
			Class <ComponentType> componentClass,
			Supplier <ComponentType> orElse);

	<ComponentType>
	Provider <ComponentType> getComponentProviderRequired (
			String componentName,
			Class <ComponentType> componentClass);

	List <String> requestComponentNames ();

	@Override
	void close ();

}
