package wbs.apn.chat.help.console;

import static wbs.utils.string.StringUtils.camelToSpaces;
import static wbs.utils.string.StringUtils.camelToUnderscore;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Provider;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.context.ConsoleContextBuilderContainer;
import wbs.console.context.ConsoleContextBuilderContainerImplementation;
import wbs.console.context.ConsoleContextType;
import wbs.console.context.SimpleConsoleContext;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.module.ConsoleModuleImplementation;
import wbs.console.object.ObjectContext;
import wbs.console.tab.ConsoleContextTab;

import wbs.framework.builder.Builder;
import wbs.framework.builder.Builder.MissingBuilderBehaviour;
import wbs.framework.builder.BuilderComponent;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.platform.object.create.ObjectCreatePageSpec;
import wbs.platform.object.list.ObjectListPageSpec;
import wbs.platform.object.settings.ObjectSettingsPageSpec;

import wbs.apn.chat.help.model.ChatHelpTemplateRec;

@PrototypeComponent ("chatHelpTemplateContextBuilder")
@ConsoleModuleBuilderHandler
public
class ChatHelpTemplateContextBuilder
	implements BuilderComponent {

	// singleton dependencies

	@SingletonDependency
	ChatHelpTemplateConsoleHelper chatHelpTemplateHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	// prototype dependences

	@PrototypeDependency
	Provider <SimpleConsoleContext> simpleConsoleContextProvider;

	@PrototypeDependency
	Provider <ConsoleContextTab> contextTabProvider;

	@PrototypeDependency
	Provider <ConsoleContextType> contextTypeProvider;

	@PrototypeDependency
	Provider <ObjectContext> objectContextProvider;

	@PrototypeDependency
	Provider <ObjectCreatePageSpec> objectCreatePageSpecProvider;

	@PrototypeDependency
	Provider <ObjectListPageSpec> objectListPageSpecProvider;

	@PrototypeDependency
	Provider <ObjectSettingsPageSpec> objectSettingsPageSpecProvider;

	// builder

	@BuilderParent
	ConsoleContextBuilderContainer <ChatHelpTemplateRec> container;

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

			buildContextTypes ();
			buildContexts ();
			buildParentTab ();
			buildListPage ();
			buildCreatePage ();
			buildSettingsPage ();

			ConsoleContextBuilderContainer <ChatHelpTemplateRec> listContainer =
				new ConsoleContextBuilderContainerImplementation
					<ChatHelpTemplateRec> ()

				.taskLogger (
					container.taskLogger ())

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
				taskLogger,
				listContainer,
				listChildren,
				consoleModule,
				MissingBuilderBehaviour.error);

			ConsoleContextBuilderContainer <ChatHelpTemplateRec> objectContainer =
				new ConsoleContextBuilderContainerImplementation
					<ChatHelpTemplateRec> ()

				.taskLogger (
					container.taskLogger ())

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
				taskLogger,
				objectContainer,
				objectBuilders,
				consoleModule,
				MissingBuilderBehaviour.error);

		}

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
				ImmutableMap.<String, Object> of (
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
				ImmutableMap.<String, Object> of (
					"chatHelpTemplateType",
					typeCamel)));

	}

	void buildParentTab () {

		consoleModule.addContextTab (
			container.taskLogger (),
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
