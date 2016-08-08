package wbs.clients.apn.chat.help.console;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import javax.inject.Inject;
import javax.inject.Provider;

import com.google.common.collect.ImmutableList;

import wbs.console.annotations.ConsoleMetaModuleBuilderHandler;
import wbs.console.context.ConsoleContextMetaBuilderContainer;
import wbs.console.context.ConsoleContextRootExtensionPoint;
import wbs.console.module.ConsoleMetaModuleImplementation;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;

@PrototypeComponent ("chatHelpTemplateContextMetaBuilder")
@ConsoleMetaModuleBuilderHandler
public
class ChatHelpTemplateContextMetaBuilder {

	// prototype dependencies

	@Inject
	Provider<ConsoleContextRootExtensionPoint> rootExtensionPointProvider;

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

	@BuildMethod
	public
	void build (
			Builder builder) {

		setDefaults ();

		buildListExtensionPoint ();
		buildObjectExtensionPoint ();

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
