package wbs.console.combo;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.camelToSpaces;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import lombok.NonNull;

import wbs.console.component.ConsoleComponentBuilderComponent;
import wbs.console.component.ConsoleComponentBuilderContext;
import wbs.console.part.ProviderPagePartFactory;
import wbs.console.tab.TabContextResponder;

import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentManager;
import wbs.framework.component.registry.ComponentDefinition;
import wbs.framework.component.registry.ComponentRegistryBuilder;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("contextTabResponderPageComponentBuilder")
public
class ContextTabResponderPageComponentBuilder
	implements ConsoleComponentBuilderComponent {

	// singleton dependencies

	@SingletonDependency
	ComponentManager componentManager;

	@ClassSingletonDependency
	LogContext logContext;

	// builder

	@BuilderParent
	ConsoleComponentBuilderContext context;

	@BuilderSource
	ContextTabResponderPageSpec spec;

	@BuilderTarget
	ComponentRegistryBuilder target;

	// build

	@BuildMethod
	@Override
	public
	void build (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Builder <TaskLogger> builder) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"build");

		) {

			String tabName =
				ifNull (
					spec.tabName (),
					stringFormat (
						"%s.%s",
						context.pathPrefix (),
						spec.name ()));

			String title =
				ifNull (
					spec.pageTitle (),
					capitalise (
						camelToSpaces (
							spec.name ())));

			String pagePartName =
				ifNull (
					spec.pagePartName (),
					stringFormat (
						"%s%sPart",
						context.existingComponentNamePrefix (),
						capitalise (
							spec.name ())));

			String pagePartFactoryName =
				stringFormat (
					"%s%sPartFactory",
					context.newComponentNamePrefix (),
					capitalise (
						spec.name ()));

			target.registerDefinition (
				taskLogger,
				new ComponentDefinition ()

				.name (
					pagePartFactoryName)

				.scope (
					"singleton")

				.componentClass (
					ProviderPagePartFactory.class)

				.hide (
					true)

				.addReferenceProperty (
					"pagePartProvider",
					"prototype",
					pagePartName)

			);

			target.registerDefinition (
				taskLogger,
				new ComponentDefinition ()

				.nameFormat (
					"%s%sResponder",
					context.newComponentNamePrefix (),
					capitalise (
						spec.name ()))

				.componentClass (
					TabContextResponder.class)

				.scope (
					"prototype")

				.hide (
					true)

				.addValueProperty (
					"tab",
					optionalOf (
						tabName))

				.addValueProperty (
					"title",
					optionalOf (
						title))

				.addReferenceProperty (
					"pagePartFactory",
					"singleton",
					pagePartFactoryName)

			);

		}

	}

}
