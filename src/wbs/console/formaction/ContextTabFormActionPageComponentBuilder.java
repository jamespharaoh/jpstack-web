package wbs.console.formaction;

import static wbs.utils.etc.NullUtils.anyIsNotNull;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalIf;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.camelToSpaces;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.hyphenToCamel;
import static wbs.utils.string.StringUtils.hyphenToCamelCapitalise;
import static wbs.utils.string.StringUtils.hyphenToSpaces;
import static wbs.utils.string.StringUtils.stringFormat;

import lombok.NonNull;

import wbs.console.component.ConsoleComponentBuilderComponent;
import wbs.console.component.ConsoleComponentBuilderContext;
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

@PrototypeComponent ("contextTabFormActionPageComponentBuilder")
public
class ContextTabFormActionPageComponentBuilder
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
	ContextTabFormActionPageSpec spec;

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

			target.registerDefinition (
				taskLogger,
				new ComponentDefinition ()

				.nameFormat (
					"%s%sFormPartFactory",
					context.newComponentNamePrefix (),
					capitalise (
						spec.name ()))

				.componentClass (
					ConsoleFormActionPartFactory.class)

				.scope (
					"singleton")

				.hide (
					true)

				.addValueProperty (
					"name",
					optionalOf (
						"action"))

				.addValuePropertyFormat (
					"heading",
					"%s %s",
					capitalise (
						hyphenToSpaces (
							context.consoleModule ().name ())),
					camelToSpaces (
						spec.name ()))

				.addReferencePropertyFormat (
					"helperProvider",
					"prototype",
					"%s%sFormActionHelper",
					context.newComponentNamePrefix (),
					capitalise (
						ifNull (
							spec.helperName (),
							spec.name ())))

				.addReferencePropertyFormat (
					"actionFormType",
					"singleton",
					"%s%sFormType",
					hyphenToCamel (
						context.consoleModule ().name ()),
					hyphenToCamelCapitalise (
						ifNull (
							spec.actionFormTypeName (),
							stringFormat (
								"%sAction",
								spec.name ()))))

				.addValueProperty (
					"helpText",
					optionalFromNullable (
						spec.helpText ()))

				.addValueProperty (
					"submitLabel",
					optionalFromNullable (
						spec.submitLabel ()))

				.addValuePropertyFormat (
					"localFile",
					"/%s.%s",
					context.pathPrefix (),
					spec.name ())

				.addValueProperty (
					"historyHeading",
					optionalFromNullable (
						spec.historyHeading ()))

				.addReferenceProperty (
					"historyFormType",
					"singleton",
					optionalIf (
						anyIsNotNull (
							spec.historyHeading (),
							spec.historyFormTypeName ()),
						() -> stringFormat (
							"%s%sFormType",
							hyphenToCamel (
								context.consoleModule ().name ()),
							hyphenToCamelCapitalise (
								ifNull (
									spec.historyFormTypeName (),
									stringFormat (
										"%sHistory",
										spec.name ()))))))

			);

			target.registerDefinition (
				taskLogger,
				new ComponentDefinition ()

				.nameFormat (
					"%s%sFormResponder",
					context.newComponentNamePrefix (),
					capitalise (
						spec.name ()))

				.componentClass (
					TabContextResponder.class)

				.scope (
					"prototype")

				.hide (
					true)

				.addValuePropertyFormat (
					"tab",
					"%s.%s",
					context.pathPrefix (),
					spec.name ())

				.addValuePropertyFormat (
					"title",
					"%s %s",
					capitalise (
						context.friendlyName ()),
					camelToSpaces (
						spec.name ()))

				.addReferencePropertyFormat (
					"pagePartFactory",
					"singleton",
					"%s%sFormPartFactory",
					context.newComponentNamePrefix (),
					capitalise (
						spec.name ()))

			);

		}

	}

}
