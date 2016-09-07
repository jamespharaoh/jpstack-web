package wbs.framework.component.registry;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;

import wbs.framework.component.manager.ComponentManager;

public
interface ComponentRegistry {

	// accessors

	Collection <ComponentDefinition> all ();

	Optional <ComponentDefinition> byName (
			String componentName);

	ComponentDefinition byNameRequired (
			String componentName);

	boolean hasName (
			String componentName);

	Map <String, ComponentDefinition> singletonsByClass (
			Class <?> targetClass);

	Map <String, ComponentDefinition> prototypesByClass (
			Class <?> targetClass);

	List <ComponentDefinition> withAnnotation (
			Class <? extends Annotation> annotationClass);

	// component registration

	ComponentRegistry registerDefinition (
			ComponentDefinition componentDefinition);

	ComponentRegistry registerUnmanagedSingleton (
			String key,
			Object value);

	ComponentRegistry registerXmlFilename (
			String filename);

	// request components (messy)

	ComponentRegistry addRequestComponentName (
			String name);

	List <String> requestComponentNames ();

	// build

	ComponentManager build ();

}
