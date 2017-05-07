package wbs.framework.component.registry;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataName;

@Accessors (fluent = true)
@DataClass
public
class ComponentDefinition {

	@DataName
	@Getter @Setter
	String name;

	@DataAttribute
	@Getter @Setter
	String scope;

	@DataAttribute
	@Getter @Setter
	Class <?> componentClass;

	@DataAttribute
	@Getter @Setter
	Class <? extends ComponentFactory <?>> factoryClass;

	@DataAttribute
	@Getter @Setter
	Boolean hide = false;

	@DataAttribute
	@Getter @Setter
	Boolean owned = true;

	@DataAttribute
	@Getter @Setter
	Boolean fromAnnotatedClass = false;

	@DataChildren
	@Getter @Setter
	Map <String, Object> valueProperties =
		new LinkedHashMap<> ();

	@DataChildren
	@Getter @Setter
	Map <String, String> referenceProperties =
		new LinkedHashMap<> ();

	@DataChildren
	@Getter @Setter
	List <InjectedProperty> injectedProperties =
		new ArrayList<> ();

	@DataChildren
	@Getter @Setter
	List <Method> normalSetupMethods =
		new ArrayList<> ();

	@DataChildren
	@Getter @Setter
	List <Method> lateSetupMethods =
		new ArrayList<> ();

	@DataChildren
	@Getter @Setter
	List <Method> normalTeardownMethods =
		new ArrayList<> ();

	@DataChildren
	@Getter @Setter
	Set <String> strongDependencies =
		new HashSet<> ();

	@DataChildren
	@Getter @Setter
	Set <String> weakDependencies =
		new HashSet<> ();

	public
	ComponentDefinition addValueProperty (
			@NonNull String name,
			@NonNull Object value) {

		valueProperties.put (
			name,
			value);

		return this;

	}

	public
	ComponentDefinition addReferenceProperty (
			@NonNull String name,
			@NonNull String referencedComponentName) {

		referenceProperties.put (
			name,
			referencedComponentName);

		return this;

	}

}
