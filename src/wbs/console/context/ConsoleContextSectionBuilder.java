package wbs.console.context;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.StringUtils.camelToSpaces;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.joinWithoutSeparator;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Iterables;

import lombok.NonNull;

import wbs.console.helper.core.ConsoleHelper;
import wbs.console.module.ConsoleMetaManager;
import wbs.console.module.ConsoleModuleBuilderComponent;
import wbs.console.module.ConsoleModuleImplementation;
import wbs.console.tab.ConsoleContextTab;

import wbs.framework.builder.Builder;
import wbs.framework.builder.Builder.MissingBuilderBehaviour;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("consoleContextSectionBuilder")
public
class ConsoleContextSectionBuilder <
	ObjectType extends Record <ObjectType>
>
	implements ConsoleModuleBuilderComponent {

	// singleton dependencies

	@SingletonDependency
	ConsoleMetaManager consoleMetaManager;

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <ConsoleContextTab> contextTabProvider;

	@PrototypeDependency
	ComponentProvider <ConsoleContextType> contextTypeProvider;

	@PrototypeDependency
	ComponentProvider <SimpleConsoleContext> simpleConsoleContextProvider;

	// builder

	@BuilderParent
	ConsoleContextBuilderContainer <ObjectType> container;

	@BuilderSource
	ConsoleContextSectionSpec spec;

	@BuilderTarget
	ConsoleModuleImplementation consoleModule;

	// state

	ConsoleHelper<ObjectType> consoleHelper;

	String name;
	String structuralName;
	String aliasOf;
	String label;
	String contextTypeName;
	String tabName;
	String tabTarget;

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

			setDefaults ();

			buildContextTypes (
				taskLogger);

			for (
				ResolvedConsoleContextExtensionPoint resolvedExtensionPoint
					: consoleMetaManager.resolveExtensionPoint (
						taskLogger,
						container.extensionPointName ())
			) {

				buildResolvedContexts (
					taskLogger,
					resolvedExtensionPoint);

				buildContextTabs (
					taskLogger,
					resolvedExtensionPoint);

			}

			ConsoleContextBuilderContainer <ObjectType>
				nextContextBuilderContainer =
					new ConsoleContextBuilderContainerImplementation <
						ObjectType
					> ()

				.consoleModule (
					container.consoleModule ())

				.consoleHelper (
					consoleHelper)

				.structuralName (
					structuralName)

				.extensionPointName (
					"section:" + contextTypeName)

				.pathPrefix (
					contextTypeName)

				.existingBeanNamePrefix (
					stringFormat (
						"%s%s",
						container.existingBeanNamePrefix (),
						capitalise (
							aliasOf)))

				.newBeanNamePrefix (
					stringFormat (
						"%s%s",
						container.newBeanNamePrefix (),
						capitalise (
							name)))

				.tabLocation (
					"end")

				.friendlyName (
					container.friendlyName ());

			builder.descend (
				taskLogger,
				nextContextBuilderContainer,
				spec.children (),
				consoleModule,
				MissingBuilderBehaviour.error);

		}

	}

	void buildContextTypes (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"buildContextTypes");

		) {

			consoleModule.addContextType (
				contextTypeProvider.provide (
					taskLogger)

				.name (
					contextTypeName)

			);

		}

	}

	void buildResolvedContexts (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ResolvedConsoleContextExtensionPoint extensionPoint) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"buildResolvedContexts");

		) {

			Map <String, Object> stuffMap =
				new HashMap<> ();

			for (
				ConsoleContextStuffSpec contextStuffSpec
					: Iterables.filter (
						spec.children (),
						ConsoleContextStuffSpec.class)
			) {

				stuffMap.put (
					contextStuffSpec.name (),
					contextStuffSpec.value ());

			}

			for (
				String parentContextName
					: extensionPoint.parentContextNames ()
			) {

					String resolvedContextName =
						stringFormat (
							"%s.%s",
							parentContextName,
							spec.name ());

					boolean link =
						resolvedContextName.startsWith ("link:");

					String resolvedPathPrefix =
						joinWithoutSeparator (
							"/",
							link
								? resolvedContextName.substring (5)
								: resolvedContextName);

				consoleModule.addContext (
					simpleConsoleContextProvider.provide (
						taskLogger)

					.name (
						stringFormat (
							"%s.%s",
							parentContextName,
							spec.name ()))

					.typeName (
						contextTypeName)

					.pathPrefix (
						resolvedPathPrefix)

					.global (
						! link)

					.title (
						label)

					.parentContextName (
						parentContextName)

					.parentContextTabName (
						tabName)

					.stuff (
						stuffMap));

			}

		}

	}

	void buildContextTabs (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ResolvedConsoleContextExtensionPoint extensionPoint) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"buildContextTabs");

		) {

			consoleModule.addContextTab (
				taskLogger,
				container.tabLocation (),

				contextTabProvider.provide (
					taskLogger)

					.name (
						tabName)

					.defaultLabel (
						label)

					.localFile (
						tabTarget),

				extensionPoint.contextTypeNames ());

		}

	}

	void setDefaults () {

		consoleHelper =
			container.consoleHelper ();

		name =
			spec.name ();

		structuralName =
			stringFormat (
				"%s.%s",
				container.structuralName (),
				name);

		aliasOf =
			ifNull (
				spec.aliasOf (),
				name);

		label =
			ifNull (
				spec.label (),
				camelToSpaces (
					name));

		contextTypeName =
			structuralName;

		tabName =
			structuralName;

		tabTarget =
			stringFormat (
				"type:%s",
				contextTypeName);

	}

}
