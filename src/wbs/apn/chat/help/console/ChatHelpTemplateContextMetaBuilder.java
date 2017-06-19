package wbs.apn.chat.help.console;

import static wbs.utils.string.StringUtils.stringFormat;

import com.google.common.collect.ImmutableList;

import lombok.NonNull;

import wbs.console.context.ConsoleContextMetaBuilderContainer;
import wbs.console.context.ConsoleContextRootExtensionPoint;
import wbs.console.module.ConsoleMetaModuleBuilderComponent;
import wbs.console.module.ConsoleMetaModuleImplementation;

import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("chatHelpTemplateContextMetaBuilder")
public
class ChatHelpTemplateContextMetaBuilder
	implements ConsoleMetaModuleBuilderComponent {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <ConsoleContextRootExtensionPoint>
		rootExtensionPointProvider;

	// builder

	@BuilderParent
	ConsoleContextMetaBuilderContainer container;

	@BuilderSource
	ChatHelpTemplateContextSpec spec;

	@BuilderTarget
	ConsoleMetaModuleImplementation metaModule;

	// state

	String typeCamel;

	String contextTypeName;
	String contextTypeNamePlural;
	String contextTypeNameCombo;

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

			setDefaults ();

			buildListExtensionPoint (
				taskLogger);

			buildObjectExtensionPoint (
				taskLogger);

		}

	}

	void buildListExtensionPoint (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"buildListExtensionPoint");

		) {

			metaModule.addExtensionPoint (
				rootExtensionPointProvider.provide (
					taskLogger)

				.name (
					contextTypeName + ":list")

				.contextTypeNames (
					ImmutableList.<String>of (
						contextTypeNamePlural,
						contextTypeNameCombo))

				.contextLinkNames (
					ImmutableList.<String>of (
						contextTypeName))

				.parentContextNames (
					ImmutableList.<String>of ())

			);

		}

	}

	void buildObjectExtensionPoint (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"buildObjectExtensionPoint");

		) {

			metaModule.addExtensionPoint (
				rootExtensionPointProvider.provide (
					taskLogger)

				.name (
					contextTypeName + ":object")

				.contextTypeNames (
					ImmutableList.<String>of (
						contextTypeNameCombo,
						contextTypeName))

				.contextLinkNames (
					ImmutableList.<String>of (
						contextTypeName))

				.parentContextNames (
					ImmutableList.<String>of ())

			);

		}

	}

	// defaults

	void setDefaults () {

		typeCamel =
			spec.type ();

		contextTypeName =
			stringFormat (
				"chatHelpTemplate.%s",
				typeCamel);

		contextTypeNamePlural =
			stringFormat (
				"chatHelpTemplates.%s",
				typeCamel);

		contextTypeNameCombo =
			stringFormat (
				"chatHelpTemplate+.%s",
				typeCamel);

	}

}
