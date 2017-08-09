package wbs.console.combo;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.OptionalUtils.optionalOf;
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

@PrototypeComponent ("contextTabResponderComponentBuilder")
public
class ContextTabResponderComponentBuilder
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
	ContextTabResponderSpec spec;

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
				spec.tabName ();

			String title =
				ifNull (
					spec.title (),
					capitalise (
						spec.name ()));

			String partName =
				ifNull (
					spec.pagePartName (),
					stringFormat (
						"%s%sPart",
						context.existingComponentNamePrefix (),
						capitalise (
							spec.name ())));

			String partFactoryName =
				stringFormat (
					"%s%sPartFactory",
					context.existingComponentNamePrefix (),
					capitalise (
						spec.name ()));

			String responderName =
				stringFormat (
					"%s%sResponder",
					context.existingComponentNamePrefix (),
					capitalise (
						spec.name ()));

			target.registerDefinition (
				taskLogger,
				new ComponentDefinition ()

				.name (
					partFactoryName)

				.componentClass (
					ProviderPagePartFactory.class)

				.scope (
					"singleton")

				.hide (
					true)

				.addReferenceProperty (
					"pagePartProvider",
					"prototype",
					partName)

			);

			target.registerDefinition (
				taskLogger,
				new ComponentDefinition ()

				.name (
					responderName)

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
					partFactoryName)

			);

		}

	}

}
