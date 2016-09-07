package wbs.framework.component.xml;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.List;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;

import wbs.framework.component.registry.ComponentDefinition;
import wbs.framework.component.registry.ComponentRegistryImplementation;
import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@DataClass ("components")
@Log4j
public
class ComponentsSpec {

	@DataChildren (direct = true)
	@Getter @Setter
	List <ComponentsComponentSpec> components;

	@SneakyThrows (Exception.class)
	public
	int register (
			@NonNull ComponentRegistryImplementation registry) {

		int errors = 0;

		for (ComponentsComponentSpec component
				: components) {

			Class<?> componentClass = null;

			try {

				componentClass =
					Class.forName (component.className ());

			} catch (ClassNotFoundException exception) {

				log.error (
					stringFormat (
						"No such class %s specified as bean class for %s",
						component.className (),
						component.name ()));

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

				log.error (
					stringFormat (
						"No such class %s specified as factory for %s",
						component.factoryClassName (),
						component.name ()));

				errors ++;

			} catch (ClassCastException exception) {

				log.error (
					stringFormat (
						"Factory class %s for %s is not a BeanFactory",
						component.factoryClassName (),
						component.name ()));

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
				componentDefinition);

		}

		return errors;

	}

}
