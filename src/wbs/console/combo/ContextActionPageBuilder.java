package wbs.console.combo;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

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
import wbs.framework.component.manager.ComponentManager;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.web.responder.WebResponder;

@PrototypeComponent ("contextActionPageBuilder")
public
class ContextActionPageBuilder <
	ObjectType extends Record <ObjectType>
>
	implements ConsoleModuleBuilderComponent {

	// singleton dependencies

	@SingletonDependency
	ComponentManager componentManager;

	@SingletonDependency
	ConsoleMetaManager consoleMetaManager;

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <ConsoleFile> consoleFileProvider;

	// builder

	@BuilderSource
	ContextActionPageSpec contextActionPageSpec;

	@BuilderParent
	ConsoleContextBuilderContainer <ObjectType> container;

	@BuilderTarget
	ConsoleModuleImplementation consoleModule;

	// state

	String name;
	String fileName;
	String actionName;
	String responderName;

	ComponentProvider <WebResponder> responderProvider;

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

			responderProvider =
				componentManager.getComponentProviderRequired (
					taskLogger,
					responderName,
					WebResponder.class);

			for (
				ResolvedConsoleContextExtensionPoint resolvedExtensionPoint
					: consoleMetaManager.resolveExtensionPoint (
						container.extensionPointName ())
			) {

				consoleModule.addContextFile (
					fileName,
					consoleFileProvider.provide (
						taskLogger)

						.getResponderProvider (
							responderProvider)

						.postActionName (
							taskLogger,
							actionName),

					resolvedExtensionPoint.contextTypeNames ()
				);

			}

		}

	}

	// defaults

	void setDefaults () {

		name =
			contextActionPageSpec.name ();

		fileName =
			ifNull (
				contextActionPageSpec.fileName (),
				stringFormat (
					"%s.%s",
					container.pathPrefix (),
					name));

		actionName =
			ifNull (
				contextActionPageSpec.actionName (),
				stringFormat (
					"%s%sAction",
					container.existingBeanNamePrefix (),
					capitalise (
						name)));

		responderName =
			ifNull (
				contextActionPageSpec.responderName (),
				stringFormat (
					"%s%sResponder",
					container.newBeanNamePrefix (),
					capitalise (
						name)));

		/*
		responderBeanName =
			ifNull (
				contextActionPageSpec.responderBeanName (),
				stringFormat (
					"%s%sResponder",
					container.existingBeanNamePrefix (),
					capitalise (
						name)));
		*/

	}

}
