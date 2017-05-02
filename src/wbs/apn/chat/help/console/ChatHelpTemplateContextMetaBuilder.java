package wbs.apn.chat.help.console;

import static wbs.utils.string.StringUtils.stringFormat;

import javax.inject.Provider;

import com.google.common.collect.ImmutableList;

import lombok.NonNull;

import wbs.console.annotations.ConsoleMetaModuleBuilderHandler;
import wbs.console.context.ConsoleContextMetaBuilderContainer;
import wbs.console.context.ConsoleContextRootExtensionPoint;
import wbs.console.module.ConsoleMetaModuleImplementation;

import wbs.framework.builder.Builder;
import wbs.framework.builder.BuilderComponent;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("chatHelpTemplateContextMetaBuilder")
@ConsoleMetaModuleBuilderHandler
public
class ChatHelpTemplateContextMetaBuilder
	implements BuilderComponent {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <ConsoleContextRootExtensionPoint> rootExtensionPointProvider;

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

			buildListExtensionPoint ();
			buildObjectExtensionPoint ();

		}

	}

	void buildListExtensionPoint () {

		metaModule.addExtensionPoint (
			rootExtensionPointProvider.get ()

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

	void buildObjectExtensionPoint () {

		metaModule.addExtensionPoint (
			rootExtensionPointProvider.get ()

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
