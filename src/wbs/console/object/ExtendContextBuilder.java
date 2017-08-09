package wbs.console.object;

import static wbs.utils.etc.LogicUtils.ifNotNullThenElse;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;

import java.util.List;

import lombok.NonNull;

import wbs.console.context.ConsoleContextBuilderContainer;
import wbs.console.context.ConsoleContextBuilderContainerImplementation;
import wbs.console.context.ResolvedConsoleContextExtensionPoint;
import wbs.console.helper.core.ConsoleHelper;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.module.ConsoleMetaManager;
import wbs.console.module.ConsoleModuleBuilderComponent;
import wbs.console.module.ConsoleModuleImplementation;
import wbs.console.module.SimpleConsoleBuilderContainer;

import wbs.framework.builder.Builder;
import wbs.framework.builder.Builder.MissingBuilderBehaviour;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("extendContextBuilder")
public
class ExtendContextBuilder <
	ObjectType extends Record <ObjectType>
>
	implements ConsoleModuleBuilderComponent {

	// singleton dependencies

	@SingletonDependency
	ConsoleMetaManager consoleMetaManager;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	// builder

	@BuilderParent
	SimpleConsoleBuilderContainer container;

	@BuilderSource
	ExtendContextSpec spec;

	@BuilderTarget
	ConsoleModuleImplementation consoleModule;

	// state

	String name;
	String baseName;
	String extensionPointName;

	ConsoleHelper <ObjectType> consoleHelper;

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

			buildChildren (
				taskLogger,
				builder);

		}

	}

	void buildChildren (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Builder <TaskLogger> builder) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"buildChildren");

		) {

			List <ResolvedConsoleContextExtensionPoint>
				resolvedExtensionPoints =
					consoleMetaManager.resolveExtensionPoint (
						taskLogger,
						extensionPointName);

			if (resolvedExtensionPoints == null) {

				taskLogger.warningFormat (
					"Extend context %s in %s doesn't resolve",
					extensionPointName,
					spec.consoleSpec ().name ());

				return;

			}

			ConsoleContextBuilderContainer <ObjectType> nextBuilderContainer =
				new ConsoleContextBuilderContainerImplementation <ObjectType> ()

				.consoleModule (
					container.consoleModule ())

				.consoleHelper (
					consoleHelper)

				.structuralName (
					baseName)

				.extensionPointName (
					extensionPointName)

				.pathPrefix (
					baseName)

				.newBeanNamePrefix (
					consoleHelper.objectName ())

				.existingBeanNamePrefix (
					consoleHelper.objectName ())

				.tabLocation (
					extensionPointName)

				.friendlyName (
					consoleHelper.friendlyName ());

			builder.descend (
				taskLogger,
				nextBuilderContainer,
				spec.children (),
				consoleModule,
				MissingBuilderBehaviour.error);

		}

	}

	// defaults

	void setDefaults () {

		name =
			spec.name ();

		baseName =
			spec.baseName ();

		extensionPointName =
			spec.extensionPointName ();

		ConsoleHelper <ObjectType> consoleHelperTemp =
			ifNotNullThenElse (
				spec.objectName (),
				() -> genericCastUnchecked (
					objectManager.consoleHelperForNameRequired (
						spec.objectName ())),
				() -> null);

		consoleHelper =
			consoleHelperTemp;

	}

}
