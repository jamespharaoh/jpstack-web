package wbs.apn.chat.help.console;

import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.string.StringUtils.camelToSpaces;
import static wbs.utils.string.StringUtils.camelToUnderscore;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.console.component.ConsoleComponentBuilderComponent;
import wbs.console.component.ConsoleComponentBuilderContext;
import wbs.console.component.ConsoleComponentBuilderContextImplementation;

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
import wbs.framework.component.manager.ComponentManager;
import wbs.framework.component.registry.ComponentRegistryBuilder;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.platform.object.create.ObjectCreatePageSpec;
import wbs.platform.object.list.ObjectListPageSpec;
import wbs.platform.object.settings.ObjectSettingsPageSpec;

@PrototypeComponent ("chatHelpTemplateContextComponentBuilder")
public
class ChatHelpTemplateContextComponentBuilder
	implements ConsoleComponentBuilderComponent {

	// singleton dependencies

	@SingletonDependency
	ComponentManager componentManager;

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependences

/*
	@PrototypeDependency
	Provider <SimpleConsoleContext> simpleConsoleContextProvider;

	@PrototypeDependency
	Provider <ConsoleContextTab> contextTabProvider;

	@PrototypeDependency
	Provider <ConsoleContextType> contextTypeProvider;

	@PrototypeDependency
	Provider <ObjectContext> objectContextProvider;
*/

	@PrototypeDependency
	Provider <ObjectCreatePageSpec> objectCreatePageSpecProvider;

	@PrototypeDependency
	Provider <ObjectListPageSpec> objectListPageSpecProvider;

	@PrototypeDependency
	Provider <ObjectSettingsPageSpec> objectSettingsPageSpecProvider;

	// builder

	@BuilderParent
	ConsoleComponentBuilderContext parentContext;

	@BuilderSource
	ChatHelpTemplateContextSpec spec;

	@BuilderTarget
	ComponentRegistryBuilder target;

	// state

/*
	String structuralName;
*/

	String typeCamel;
	String typeCode;

/*
	String contextName;
	String contextNamePlural;
*/

	String contextTypeName;
	String contextTypeNamePlural;
	String contextTypeNameCombined;

	String componentName;
/*
	String objectTitle;

	List <String> parentContextTypeNames;
	String parentContextName;
	String parentContextTabName;
	String parentContextTabLocation;
	String parentContextTabLabel;
	String parentContextTabLocalFile;
	String parentContextPrivKey;
*/

	String settingsResponderName;

	List <Object> listChildren =
		new ArrayList<> ();

	List <Object> objectChildren =
		new ArrayList<> ();

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

			setDefaults (
				taskLogger);

			buildListPage (
				taskLogger);

			buildCreatePage (
				taskLogger);

			buildSettingsPage (
				taskLogger);

			ConsoleComponentBuilderContext childContext =
				new ConsoleComponentBuilderContextImplementation ()

				.consoleModule (
					parentContext.consoleModule ())

				.structuralName (
					contextTypeName)

				.pathPrefix (
					contextTypeName)

				.newComponentNamePrefix (
					componentName)

				.existingComponentNamePrefix (
					componentName)

				.friendlyName (
					camelToSpaces (
						componentName))

				.objectType (
					"chat-help-template")

			;

			builder.descend (
				taskLogger,
				childContext,
				listChildren,
				target,
				MissingBuilderBehaviour.error);

			builder.descend (
				taskLogger,
				childContext,
				objectChildren,
				target,
				MissingBuilderBehaviour.error);

		}

	}

	private
	void buildListPage (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"buildListPage");

		) {

			listChildren.add (
				objectListPageSpecProvider.get ()

				.consoleSpec (
					spec.consoleSpec ())

				.typeCode (
					typeCode)

				.formTypeName (
					"list")

				.targetContextTypeName (
					stringFormat (
						"chatHelpTemplate+.%s",
						typeCamel))

				.listTabs (
					emptyList ())

				.listTabsByName (
					emptyMap ())

				.listBrowsers (
					emptyList ())

				.listBrowsersByFieldName (
					emptyMap ())

			);

		}

	}

	private
	void buildCreatePage (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"buildCreatePage");

		) {

			listChildren.add (
				objectCreatePageSpecProvider.get ()

				.consoleSpec (
					spec.consoleSpec ())

				.typeCode (
					typeCode)

				.formTypeName (
					"create")

				.targetContextTypeName (
					contextTypeNameCombined)

				.targetResponderName (
					settingsResponderName));

		}

	}

	private
	void buildSettingsPage (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"buildSettingsPage");

		) {

			objectChildren.add (
				objectSettingsPageSpecProvider.get ()

				.consoleSpec (
					spec.consoleSpec ())

				.formTypeName (
					"settings")

				.listContextTypeName (
					contextTypeNamePlural));

		}

	}

	private
	void setDefaults (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setDefaults");

		) {

			typeCamel =
				spec.type ();

			typeCode =
				camelToUnderscore (
					typeCamel);

/*
			structuralName =
				stringFormat (
					"chat.settings.template.%s",
					typeCamel);

			contextName =
				stringFormat (
					"chat.settings.template.%s",
					typeCamel);

			contextNamePlural =
				stringFormat (
					"chat.settings.templates.%s",
					typeCamel);
*/

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

			componentName =
				stringFormat (
					"chatHelpTemplate%s",
					capitalise (
						typeCamel));

/*
			objectTitle =
				stringFormat (
					"%s template {chatHelpTemplateName}",
					capitalise (
						camelToSpaces (
							typeCamel)));

			parentContextTypeNames =
				ImmutableList.<String> of (
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
*/

			settingsResponderName =
				stringFormat (
					"chatHelpTemplate%sSettingsResponder",
					capitalise (
						typeCamel));

		}

	}

}
