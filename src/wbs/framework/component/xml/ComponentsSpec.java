package wbs.framework.component.xml;

import static wbs.utils.etc.Misc.doesNotImplement;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.TypeUtils.classForName;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;

import java.util.List;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.registry.ComponentDefinition;
import wbs.framework.component.registry.ComponentRegistryImplementation;
import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
@DataClass ("components")
public
class ComponentsSpec {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// state

	@DataChildren (direct = true)
	@Getter @Setter
	List <ComponentsComponentSpec> components;

	// implementation

	public
	void register (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ComponentRegistryImplementation registry) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"register");

		) {

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

					return;

				}

				// determine factory class

				Class <? extends ComponentFactory <?>> factoryClass;

				if (
					isNotNull (
						component.factoryClassName ())
				) {

					Optional <Class <?>> factoryClasssTempOptional =
						classForName (
							component.factoryClassName ());

					if (
						optionalIsNotPresent (
							factoryClasssTempOptional)
					) {

						taskLogger.errorFormat (
							"Factory class %s ",
							component.factoryClassName (),
							"for %s does not exist",
							component.name ());



					}

					Class <?> factoryClassTemp =
						optionalGetRequired (
							factoryClasssTempOptional);

					if (
						doesNotImplement (
							factoryClassTemp,
							ComponentFactory.class)
					) {

						taskLogger.errorFormat (
							"Factory class %s for %s is not a BeanFactory",
							component.factoryClassName (),
							component.name ());

						return;

					}

					factoryClass =
						genericCastUnchecked (
							factoryClassTemp);

				} else {

					factoryClass = null;

				}

				// create component definition

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

					componentProperty.register (
						taskLogger,
						componentDefinition);

				}

				registry.registerDefinition (
					taskLogger,
					componentDefinition);

			}

		}

	}

}
