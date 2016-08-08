package wbs.clients.apn.chat.help.console;

import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.StringUtils.camelToSpaces;
import static wbs.framework.utils.etc.StringUtils.camelToUnderscore;
import static wbs.framework.utils.etc.StringUtils.capitalise;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.NonNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import wbs.clients.apn.chat.help.model.ChatHelpTemplateRec;
import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.context.ConsoleContextBuilderContainer;
import wbs.console.context.ConsoleContextBuilderContainerImplementation;
import wbs.console.context.ConsoleContextType;
import wbs.console.context.SimpleConsoleContext;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.module.ConsoleModuleImplementation;
import wbs.console.object.ObjectContext;
import wbs.console.tab.ConsoleContextTab;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.Builder.MissingBuilderBehaviour;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.platform.object.create.ObjectCreatePageSpec;
import wbs.platform.object.list.ObjectListPageSpec;
import wbs.platform.object.settings.ObjectSettingsPageSpec;

@PrototypeComponent ("chatHelpTemplateContextBuilder")
@ConsoleModuleBuilderHandler
public
class ChatHelpTemplateContextBuilder {

	// dependencies

	@Inject
	ChatHelpTemplateConsoleHelper chatHelpTemplateHelper;

	@Inject
	ConsoleObjectManager objectManager;

	// prototype dependences

	@Inject
	Provider<SimpleConsoleContext> simpleConsoleContextProvider;

	@Inject
	Provider<ConsoleContextTab> contextTabProvider;

	@Inject
	Provider<ConsoleContextType> contextTypeProvider;

	@Inject
	Provider<ObjectContext> objectContextProvider;

	@Inject
	Provider<ObjectCreatePageSpec> objectCreatePageSpecProvider;

	@Inject
	Provider<ObjectListPageSpec> objectListPageSpecProvider;

	@Inject
	Provider<ObjectSettingsPageSpec> objectSettingsPageSpecProvider;

	// builder

	@BuilderParent
	ConsoleContextBuilderContainer<ChatHelpTemplateRec> container;

	@BuilderSource
	ChatHelpTemplateContextSpec spec;

	@BuilderTarget
	ConsoleModuleImplementation consoleModule;

	// state

	String structuralName;

	String typeCamel;
	String typeCode;

	String contextName;
	String contextNamePlural;

	String contextTypeName;
	String contextTypeNamePlural;
	String contextTypeNameCombined;

	String beanName;
	String objectTitle;

	List<String> parentContextTypeNames;
	String parentContextName;
	String parentContextTabName;
	String parentContextTabLocation;
	String parentContextTabLabel;
	String parentContextTabLocalFile;
	String parentContextPrivKey;

	String settingsResponderName;

	List<Object> listChildren =
		new ArrayList<Object> ();

	List<Object> objectBuilders =
		new ArrayList<Object> ();

	// build

	@BuildMethod
	public
	void build (
			@NonNull Builder builder) {

		setDefaults ();

		buildContextTypes ();
		buildContexts ();
		buildParentTab ();
		buildListPage ();
		buildCreatePage ();
		buildSettingsPage ();

		ConsoleContextBuilderContainer<ChatHelpTemplateRec> listContainer =
			new ConsoleContextBuilderContainerImplementation<ChatHelpTemplateRec> ()

			.consoleHelper (
				chatHelpTemplateHelper)

			.structuralName (
				structuralName)

			.extensionPointName (
				contextTypeName + ":list")

			.pathPrefix (
				contextTypeName)

			.newBeanNamePrefix (
				beanName)

			.existingBeanNamePrefix (
				beanName)

			.tabLocation (
				"end")

			.friendlyName (
				camelToSpaces (
					beanName));

		builder.descend (
			listContainer,
			listChildren,
			consoleModule,
			MissingBuilderBehaviour.error);

		ConsoleContextBuilderContainer<ChatHelpTemplateRec> objectContainer =
			new ConsoleContextBuilderContainerImplementation<ChatHelpTemplateRec> ()

			.consoleHelper (
				chatHelpTemplateHelper)

			.structuralName (
				structuralName)

			.extensionPointName (
				contextTypeName + ":object")

			.pathPrefix (
				contextTypeName)

			.newBeanNamePrefix (
				beanName)

			.existingBeanNamePrefix (
				beanName)

			.tabLocation (
				"end")

			.friendlyName (
				beanName);

		builder.descend (
			objectContainer,
			objectBuilders,
			consoleModule,
			MissingBuilderBehaviour.error);

	}

	void buildContextTypes () {

		consoleModule.addContextType (
			contextTypeProvider.get ()
				.name (contextTypeNamePlural));

		consoleModule.addContextType (
			contextTypeProvider.get ()
				.name (contextTypeNameCombined));

		consoleModule.addContextType (
			contextTypeProvider.get ()
				.name (contextTypeName));

	}

	void buildContexts () {

		consoleModule.addContext (
			simpleConsoleContextProvider.get ()

			.name (
				contextNamePlural)

			.typeName (
				contextTypeNamePlural)

			.pathPrefix (
				"/" + contextNamePlural)

			.global (
				true)

			.title (
				capitalise (
					chatHelpTemplateHelper.shortNamePlural ()))

			.parentContextName (
				parentContextName)

			.parentContextTabName (
				parentContextTabName)

			.stuff (
				ImmutableMap.<String,Object>of (
					"chatHelpTemplateType",
					typeCamel)));

		consoleModule.addContext (
			objectContextProvider.get ()

			.name (
				contextName)

			.typeName (
				contextTypeNameCombined)

			.pathPrefix (
				"/" + contextName)

			.global (
				true)

			.title (
				objectTitle)

			.requestIdKey (
				chatHelpTemplateHelper.idKey ())

			.objectLookup (
				chatHelpTemplateHelper)

			.postProcessorName (
				chatHelpTemplateHelper.objectName ())

			.parentContextName (
				parentContextName)

			.parentContextTabName (
				parentContextTabName)

			.stuff (
				ImmutableMap.<String,Object>of (
					"chatHelpTemplateType",
					typeCamel)));

	}

	void buildParentTab () {

		consoleModule.addContextTab (

			parentContextTabLocation,

			contextTabProvider.get ()

				.name (
					parentContextTabName)

				.defaultLabel (
					parentContextTabLabel)

				.localFile (
					parentContextTabLocalFile)

				.privKeys (
					parentContextPrivKey),

			parentContextTypeNames);

	}

	void buildListPage () {

		listChildren.add (
			objectListPageSpecProvider.get ()

			.consoleSpec (
				spec.consoleSpec ())

			.typeCode (
				typeCode)

			.fieldsName (
				"list")

			.targetContextTypeName (
				stringFormat (
					"chatHelpTemplate+.%s",
					typeCamel))

		);

	}

	void buildCreatePage () {

		listChildren.add (
			objectCreatePageSpecProvider.get ()

			.consoleSpec (
				spec.consoleSpec ())

			.typeCode (
				typeCode)

			.fieldsName (
				"list")

			.targetContextTypeName (
				contextTypeNameCombined)

			.targetResponderName (
				settingsResponderName));

	}

	void buildSettingsPage () {

		objectBuilders.add (
			objectSettingsPageSpecProvider.get ()

			.consoleSpec (
				spec.consoleSpec ())

			.fieldsName (
				"settings")

			.listContextTypeName (
				contextTypeNamePlural));

	}

	// defaults

	void setDefaults () {

		typeCamel =
			spec.type ();

		structuralName =
			stringFormat (
				"chat.settings.template.%s",
				typeCamel);

		typeCode =
			camelToUnderscore (
				typeCamel);

		contextName =
			stringFormat (
				"chat.settings.template.%s",
				typeCamel);

		contextNamePlural =
			stringFormat (
				"chat.settings.templates.%s",
				typeCamel);

		contextTypeName =
			stringFormat (
				"chatHelpTemplate.%s",
				typeCamel);

		contextTypeNamePlural =
			stringFormat (
				"chatHelpTemplates.%s",
				typeCamel);

		contextTypeNameCombined =
			stringFormat (
				"chatHelpTemplate+.%s",
				typeCamel);

		beanName =
			stringFormat (
				"chatHelpTemplate%s",
				capitalise (typeCamel));

		objectTitle =
			stringFormat (
				"%s template {chatHelpTemplateName}",
				capitalise (
					camelToSpaces (typeCamel)));

		parentContextTypeNames =
			ImmutableList.<String>of (
				"chat.settings.templates");

		parentContextName =
			"chat.settings.templates";

		parentContextTabName =
			stringFormat (
				"chat.settings.templates.%s",
				typeCamel);

		parentContextTabLocation =
			container.tabLocation ();

		parentContextTabLabel =
			capitalise (
				camelToSpaces (
					typeCamel));

		parentContextTabLocalFile =
			stringFormat (
				"type:%s",
				contextTypeNamePlural);

		parentContextPrivKey =
			"chat.manage";

		settingsResponderName =
			stringFormat (
				"chatHelpTemplate%sSettingsResponder",
				capitalise (typeCamel));

	}

}
