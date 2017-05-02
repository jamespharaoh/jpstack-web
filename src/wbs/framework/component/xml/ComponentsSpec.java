package wbs.framework.component.xml;

import java.util.List;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.registry.ComponentDefinition;
import wbs.framework.component.registry.ComponentRegistryImplementation;
import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.logging.DefaultLogContext;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
@DataClass ("components")
public
class ComponentsSpec {

	private final static
	LogContext logContext =
		DefaultLogContext.forClass (
			ComponentsSpec.class);

	@DataChildren (direct = true)
	@Getter @Setter
	List <ComponentsComponentSpec> components;

	public
	int register (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ComponentRegistryImplementation registry) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"register");

		) {

			int errors = 0;

			for (
				ComponentsComponentSpec component
					: components
			) {

				Class <?> componentClass = null;

				try {

					componentClass =
						Class.forName (
							component.className ());

				} catch (ClassNotFoundException exception) {

					taskLogger.errorFormat (
						"No such class %s specified as bean class for %s",
						component.className (),
						component.name ());

					errors ++;

				}

				Class <? extends ComponentFactory> factoryClass = null;

				try {

					factoryClass =
						component.factoryClassName () != null
							? Class.forName (component.factoryClassName ())
								.asSubclass (ComponentFactory.class)
							: null;

				} catch (ClassNotFoundException exception) {

					taskLogger.errorFormat (
						"No such class %s specified as factory for %s",
						component.factoryClassName (),
						component.name ());

					errors ++;

				} catch (ClassCastException exception) {

					taskLogger.errorFormat (
						"Factory class %s for %s is not a BeanFactory",
						component.factoryClassName (),
						component.name ());

					errors ++;

				}

				if (errors > 0)
					continue;

				ComponentDefinition componentDefinition =
					new ComponentDefinition ()

					.name (
						component.name ())

					.componentClass (
						componentClass)

					.factoryClass (
						factoryClass)

					.hide (
						component.hide ())

					.scope (
						component.scope ());

				for (
					ComponentsComponentPropertySpec componentProperty
						: component.properties ()
				) {

					errors +=
						componentProperty.register (
							componentDefinition);

				}

				registry.registerDefinition (
					taskLogger,
					componentDefinition);

			}

			return errors;

		}

	}

}
