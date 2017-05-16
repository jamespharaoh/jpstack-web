package wbs.framework.component.xml;

import static wbs.utils.etc.OptionalUtils.optionalOf;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.registry.ComponentDefinition;
import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
@DataClass ("properties-property")
public
class ComponentsPropertiesPropertySpec
	implements ComponentsComponentPropertySpec {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// properties

	@DataAncestor
	@Getter @Setter
	ComponentsSpec beans;

	@DataParent
	@Getter @Setter
	ComponentsComponentSpec bean;

	@DataAttribute
	@Getter @Setter
	String name;

	@DataChildren (direct = true)
	@Getter @Setter
	List <ComponentPropertyValueSpec> properties =
		new ArrayList<> ();

	// public implementation

	@Override
	public
	void register (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ComponentDefinition componentDefinition) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"register");

		) {

			Properties properties =
				new Properties ();

			for (
				ComponentPropertyValueSpec propertyValue
					: this.properties
			) {

				properties.setProperty (
					propertyValue.name (),
					propertyValue.value ());

			}

			componentDefinition.addValueProperty (
				name,
				optionalOf (
					properties));

		}

	}

}
