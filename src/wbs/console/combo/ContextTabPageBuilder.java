package wbs.console.combo;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.StringUtils.camelToSpaces;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Collections;

import lombok.NonNull;

import wbs.console.context.ConsoleContextBuilderContainer;
import wbs.console.context.ResolvedConsoleContextExtensionPoint;
import wbs.console.module.ConsoleMetaManager;
import wbs.console.module.ConsoleModuleBuilderComponent;
import wbs.console.module.ConsoleModuleImplementation;
import wbs.console.responder.ConsoleFile;
import wbs.console.tab.ConsoleContextTab;
import wbs.console.tab.TabContextResponder;

import wbs.framework.builder.Builder;
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

import wbs.web.responder.WebResponder;

@PrototypeComponent ("contextTabPageBuilder")
public
class ContextTabPageBuilder <
	ObjectType extends Record <ObjectType>
> implements ConsoleModuleBuilderComponent {

	// singleton dependencies

	@SingletonDependency
	ConsoleMetaManager consoleMetaManager;

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <ConsoleFile> consoleFile;

	@PrototypeDependency
	ComponentProvider <ConsoleContextTab> contextTab;

	@PrototypeDependency
	ComponentProvider <TabContextResponder> tabContextResponder;

	// state

	String name;
	String tabName;
	String tabLabel;
	String fileName;
	Boolean hideTab;
	String title;
	String pagePartName;

	ComponentProvider <WebResponder> responderProvider;

	// builder

	@BuilderParent
	ConsoleContextBuilderContainer <ObjectType> container;

	@BuilderSource
	ContextTabPageSpec spec;

	@BuilderTarget
	ConsoleModuleImplementation consoleModule;

	// build

	@Override
	@BuildMethod
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

			setDefaults (
				taskLogger);

			buildResponder (
				taskLogger);

			for (
				ResolvedConsoleContextExtensionPoint resolvedExtensionPoint
					: consoleMetaManager.resolveExtensionPoint (
						container.extensionPointName ())
			) {

				buildTab (
					taskLogger,
					resolvedExtensionPoint);

				buildFile (
					taskLogger,
					resolvedExtensionPoint);

			}

		}

	}

	void buildTab (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ResolvedConsoleContextExtensionPoint extensionPoint) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"buildTab");

		) {

			consoleModule.addContextTab (
				taskLogger,
				container.tabLocation (),
				contextTab.provide (
					taskLogger)

					.name (
						tabName)

					.defaultLabel (
						tabLabel)

					.localFile (
						fileName),

				hideTab
					? Collections.emptyList ()
					: extensionPoint.contextTypeNames ());

		}

	}

	void buildFile (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ResolvedConsoleContextExtensionPoint extensionPoint) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"buildFile");

		) {

			consoleModule.addContextFile (
				fileName,
				consoleFile.provide (
					taskLogger)

					.getResponderProvider (
						responderProvider),

				extensionPoint.contextTypeNames ()
			);

		}

	}

	void buildResponder (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"buildResponder");

		) {

			responderProvider =
				taskLoggerNested ->
					tabContextResponder.provide (
						taskLoggerNested)

				.tab (
					tabName)

				.title (
					title)

				.pagePartName (
					pagePartName)

			;

		}

	}

	// defaults

	void setDefaults (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setDefaults");

		) {

			name =
				spec.name ();

			tabName =
				ifNull (
					spec.tabName (),
					stringFormat (
						"%s.%s",
						container.pathPrefix (),
						name));

			tabLabel =
				ifNull (
					spec.tabLabel (),
					capitalise (
						camelToSpaces (
							name)));

			fileName =
				ifNull (
					spec.fileName (),
					stringFormat (
						"%s.%s",
						container.pathPrefix (),
						name));

			title =
				ifNull (
					spec.title (),
					stringFormat (
						"%s %s",
						capitalise (container.friendlyName ()),
						camelToSpaces (name)));

			pagePartName =
				ifNull (
					spec.pagePartName (),
					stringFormat (
						"%s%sPart",
						container.existingBeanNamePrefix (),
						capitalise (name)));

			hideTab =
				spec.hideTab ();

		}

	}

}
