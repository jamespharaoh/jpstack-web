package wbs.console.object;

import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.etc.Misc.maybeList;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.camelToSpaces;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.joinWithoutSeparator;
import static wbs.utils.string.StringUtils.naivePluralise;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringStartsWithSimple;

import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Provider;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import lombok.NonNull;

import wbs.console.context.ConsoleContextBuilderContainer;
import wbs.console.context.ConsoleContextBuilderContainerImplementation;
import wbs.console.context.ConsoleContextType;
import wbs.console.context.SimpleConsoleContext;
import wbs.console.helper.core.ConsoleHelper;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.module.ConsoleMetaManager;
import wbs.console.module.ConsoleModuleBuilderComponent;
import wbs.console.module.ConsoleModuleImplementation;
import wbs.console.module.ResolvedConsoleContextLink;
import wbs.console.module.SimpleConsoleBuilderContainer;
import wbs.console.request.Cryptor;
import wbs.console.tab.ConsoleContextTab;

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
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("objectContextBuilder")
public
class ObjectContextBuilder <
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

	@SingletonDependency
	ConsoleObjectManager objectManager;

	// prototype dependencies

	@PrototypeDependency
	Provider <SimpleConsoleContext> simpleConsoleContextProvider;

	@PrototypeDependency
	Provider <ConsoleContextTab> contextTabProvider;

	@PrototypeDependency
	Provider <ConsoleContextType> contextTypeProvider;

	@PrototypeDependency
	Provider <ObjectContext> objectContextProvider;

	@PrototypeDependency
	Provider <SimpleConsoleContext> simpleContextProvider;

	// builder

	@BuilderParent
	SimpleConsoleBuilderContainer container;

	@BuilderSource
	ObjectContextSpec spec;

	@BuilderTarget
	ConsoleModuleImplementation consoleModule;

	// state

	ConsoleHelper <ObjectType> consoleHelper;

	String name;
	String structuralName;
	String componentName;

	String objectTitle;
	Optional <String> defaultFileName;

	Cryptor cryptor;

	Boolean hasListChildren;
	Boolean hasObjectChildren;

	List <String> listContextTypeNames;
	List <String> objectContextTypeNames;

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

			buildContextTypes ();

			buildSimpleContexts ();

			buildSimpleTabs (
				taskLogger);

			List <ResolvedConsoleContextLink> resolvedContextLinks =
				consoleMetaManager.resolveContextLink (
					name);

			for (
				ResolvedConsoleContextLink resolvedContextLink
					: resolvedContextLinks
			) {

				buildResolvedContexts (
					resolvedContextLink);

				buildResolvedTabs (
					taskLogger,
					resolvedContextLink);

			}

			ConsoleContextBuilderContainer <ObjectType> listContainer =
				new ConsoleContextBuilderContainerImplementation <ObjectType> ()

				.consoleModule (
					container.consoleModule ())

				.consoleHelper (
					consoleHelper)

				.structuralName (
					structuralName)

				.extensionPointName (
					name + ":list")

				.pathPrefix (
					name)

				.newBeanNamePrefix (
					componentName)

				.existingBeanNamePrefix (
					componentName)

				.tabLocation (
					"end")

				.friendlyName (
					camelToSpaces (
						componentName));

			builder.descend (
				taskLogger,
				listContainer,
				spec.listChildren (),
				consoleModule,
				MissingBuilderBehaviour.error);

			ConsoleContextBuilderContainer <ObjectType> objectContainer =
				new ConsoleContextBuilderContainerImplementation <ObjectType> ()

				.consoleModule (
					container.consoleModule ())

				.consoleHelper (
					consoleHelper)

				.structuralName (
					structuralName)

				.extensionPointName (
					name + ":object")

				.pathPrefix (
					name)

				.newBeanNamePrefix (
					componentName)

				.existingBeanNamePrefix (
					componentName)

				.tabLocation (
					"end")

				.friendlyName (
					camelToSpaces (componentName));

			builder.descend (
				taskLogger,
				objectContainer,
				spec.objectChildren (),
				consoleModule,
				MissingBuilderBehaviour.error);

		}

	}

	void buildContextTypes () {

		consoleModule.addContextType (
			contextTypeProvider.get ()

			.name (
				name + ":list")

			.defaultFileName (
				defaultFileName.orNull ()));

		consoleModule.addContextType (
			contextTypeProvider.get ()

			.name (
				name + ":combo")

			.defaultFileName (
				defaultFileName.orNull ()));

		consoleModule.addContextType (
			contextTypeProvider.get ()

			.name (
				name + ":object")

			.defaultFileName (
				defaultFileName.orNull ()));

	}

	void buildSimpleContexts () {

		consoleModule.addContext (
			simpleContextProvider.get ()

			.name (
				naivePluralise (
					name))

			.typeName (
				name + ":list")

			.pathPrefix (
				"/" + naivePluralise (
					name))

			.global (
				true)

			.title (
				capitalise (
					consoleHelper.shortNamePlural ())));

		consoleModule.addContext (
			objectContextProvider.get ()

			.name (
				name)

			.typeName (
				name + ":combo")

			.pathPrefix (
				"/" + name)

			.global (
				true)

			.title (
				objectTitle)

			.defaultFileName (
				defaultFileName)

			.requestIdKey (
				consoleHelper.idKey ())

			.objectLookup (
				consoleHelper)

			.postProcessorName (
				consoleHelper.objectName ())

			.cryptor (
				cryptor));

		consoleModule.addContext (
			objectContextProvider.get ()

			.name (
				"link:" + name)

			.typeName (
				name + ":object")

			.pathPrefix (
				"/" + name)

			.global (
				false)

			.title (
				objectTitle)

			.defaultFileName (
				defaultFileName)

			.requestIdKey (
				consoleHelper.idKey ())

			.objectLookup (
				consoleHelper)

			.postProcessorName (
				consoleHelper.objectName ())

			.cryptor (
				cryptor)

			.parentContextTabName (
				"link:" + name));

	}

	void buildSimpleTabs (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"buildSimpleTabs");

		) {

			consoleModule.addContextTab (
				taskLogger,
				"link",

				contextTabProvider.get ()

					.name (
						"link:" + name)

					.defaultLabel (
						"Object title"),

				ImmutableList.<String> of ());

			consoleModule.addContextTab (
				taskLogger,
				"end",

				contextTabProvider.get ()

					.name (
						stringFormat (
							"%s",
							name,
							":link"))

					.defaultLabel (
						capitalise (
							consoleHelper.friendlyName ())),

				emptyList ());

		}

	}

	void buildResolvedContexts (
			ResolvedConsoleContextLink resolvedContextLink) {

		for (
			String parentContextName
				: resolvedContextLink.parentContextNames ()
		) {

			String resolvedContextName =
				stringFormat (
					"%s.%s",
					parentContextName,
					resolvedContextLink.localName ());

			boolean link =
				stringStartsWithSimple (
					"link:",
					resolvedContextName);

			String resolvedPathPrefix =
				joinWithoutSeparator (
					"/",
					link
						? resolvedContextName.substring (5)
						: resolvedContextName);

			consoleModule.addContext (
				simpleConsoleContextProvider.get ()

				.name (
					naivePluralise (
						resolvedContextName))

				.typeName (
					name + ":list")

				.pathPrefix (
					naivePluralise (
						resolvedPathPrefix))

				.global (
					! link)

				.title (
					capitalise (
						consoleHelper.shortNamePlural ()))

				.parentContextName (
					parentContextName)

				.parentContextTabName (
					resolvedContextLink.tabName ()));

			consoleModule.addContext (
				objectContextProvider.get ()

				.name (
					resolvedContextName)

				.typeName (
					name + ":combo")

				.pathPrefix (
					resolvedPathPrefix)

				.global (
					! link)

				.title (
					objectTitle)

				.requestIdKey (
					consoleHelper.idKey ())

				.objectLookup (
					consoleHelper)

				.postProcessorName (
					consoleHelper.objectName ())

				.cryptor (
					cryptor)

				.parentContextName (
					parentContextName)

				.parentContextTabName (
					resolvedContextLink.tabName ()));

		}

	}

	void buildResolvedTabs (
			@NonNull TaskLogger parentTaskLogger,
			@Nonnull ResolvedConsoleContextLink consoleContextLink) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"buildResolvedTabs");

		) {

			consoleModule.addContextTab (
				taskLogger,
				consoleContextLink.tabLocation (),

				contextTabProvider.get ()

					.name (
						consoleContextLink.tabName ())

					.defaultLabel (
						consoleContextLink.tabLabel ())

					.privKeys (
						consoleContextLink.tabPrivKey ())

					.localFile (
						"type:" + name + ":list"),

				consoleContextLink.tabContextTypeNames ());

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

			consoleHelper =
				genericCastUnchecked (
					objectManager.consoleHelperForNameRequired (
						spec.objectName ()));

			name =
				spec.name ();

			structuralName =
				name;

			componentName =
				ifNull (
					spec.componentName (),
					name);

			if (componentName.contains ("_")) {

				throw new RuntimeException (
					stringFormat (
						"Object context name %s cannot be used as bean name",
						name));

			}

			objectTitle =
				ifNull (
					spec.objectTitle (),
					stringFormat (
						"%s {%s}",
						capitalise (
							consoleHelper.friendlyName ()),
						stringFormat (
							"%sName",
							consoleHelper.objectName ())));

			defaultFileName =
				Optional.fromNullable (
					spec.defaultFileName ());

			cryptor =
				spec.cryptorBeanName () != null
					? componentManager.getComponentRequired (
						taskLogger,
						spec.cryptorBeanName (),
						Cryptor.class)
					: null;

			hasListChildren =
				! spec.listChildren ().isEmpty ();

			hasObjectChildren =
				! spec.objectChildren ().isEmpty ();

			listContextTypeNames =
				ImmutableList.<String> builder ()

					.addAll (
						maybeList (
							hasListChildren,
							name + ":list"))

					.addAll (
						maybeList (
							hasObjectChildren,
							name + ":combo"))

					.build ();

			objectContextTypeNames =
				ImmutableList.<String> builder ()

					.addAll (
						maybeList (
							hasListChildren,
							name + ":combo"))

					.addAll (
						maybeList (
							hasObjectChildren,
							name + ":object"))

					.build ();

		}

	}

}
