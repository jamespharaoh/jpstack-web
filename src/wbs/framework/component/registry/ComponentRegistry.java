package wbs.framework.component.registry;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;

public
interface ComponentRegistry {

	// accessors

	Collection <ComponentDefinition> all ();

	Collection <ComponentDefinition> singletons ();

	Optional <ComponentDefinition> byName (
			String componentName);

	ComponentDefinition byNameRequired (
			String componentName);

	boolean hasName (
			String componentName);

	Map <String, ComponentDefinition> singletonsForClass (
			Class <?> componentClass);

	Map <String, ComponentDefinition> prototypesForClass (
			Class <?> componentClass);

	List <ComponentDefinition> withAnnotation (
			Class <? extends Annotation> annotationClass);

	String nameForAnnotatedClass (
			Class <?> componentClass);

	// request components (messy)

	List <String> requestComponentNames ();

}
