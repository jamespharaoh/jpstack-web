package wbs.console.combo;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.string.StringUtils.stringFormat;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.console.context.ConsoleContextBuilderContainer;
import wbs.console.context.ResolvedConsoleContextExtensionPoint;
import wbs.console.module.ConsoleMetaManager;
import wbs.console.module.ConsoleModuleBuilderComponent;
import wbs.console.module.ConsoleModuleImplementation;
import wbs.console.responder.ConsoleFile;

import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("contextFileBuilder")
public
class ContextFileBuilder <
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
	Provider <ConsoleFile> consoleFileProvider;

	// builder

	@BuilderParent
	ConsoleContextBuilderContainer <ObjectType> container;

	@BuilderSource
	ContextFileSpec spec;

	@BuilderTarget
	ConsoleModuleImplementation consoleModule;

	// state

	String name;
	String fileName;
	String getResponderName;
	String getActionName;
	String postActionName;

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

			for (
				ResolvedConsoleContextExtensionPoint resolvedExtensionPoint
					: consoleMetaManager.resolveExtensionPoint (
						container.extensionPointName ())
			) {

				buildContextFile (
					taskLogger,
					resolvedExtensionPoint);

			}

		}

	}

	void buildContextFile (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ResolvedConsoleContextExtensionPoint
				resolvedExtensionPoint) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"buildContextFile");

		) {

			consoleModule.addContextFile (
				fileName,
				consoleFileProvider.get ()

					.getResponderName (
						getResponderName)

					.getActionName (
						parentTaskLogger,
						optionalFromNullable (
							getActionName))

					.postActionName (
						parentTaskLogger,
						optionalFromNullable (
							postActionName)),

				resolvedExtensionPoint.contextTypeNames ());

		}

	}

	// defaults

	void setDefaults () {

		name =
			spec.name ();

		fileName =
			spec.fileName ();

		getResponderName =
			spec.getResponderName ();

		getActionName =
			spec.getActionName ();

		postActionName =
			spec.postActionName ();

		fileName =
			ifNull (
				spec.fileName (),
				stringFormat (
					"%s.%s",
					container.pathPrefix (),
					name));

	}

}
