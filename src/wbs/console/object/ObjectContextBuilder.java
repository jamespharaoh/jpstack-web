package wbs.console.object;

import static wbs.utils.etc.Misc.maybeList;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.camelToSpaces;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.joinWithoutSeparator;
import static wbs.utils.string.StringUtils.naivePluralise;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringStartsWithSimple;

import java.util.Collections;
import java.util.List;

import javax.inject.Provider;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.context.ConsoleContextBuilderContainer;
import wbs.console.context.ConsoleContextBuilderContainerImplementation;
import wbs.console.context.ConsoleContextType;
import wbs.console.context.SimpleConsoleContext;
import wbs.console.helper.core.ConsoleHelper;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.module.ConsoleMetaManager;
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
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentManager;
import wbs.framework.entity.record.Record;

@Log4j
@PrototypeComponent ("objectContextBuilder")
@ConsoleModuleBuilderHandler
public
class ObjectContextBuilder <
	ObjectType extends Record <ObjectType>
> {

	// singleton dependencies

	@SingletonDependency
	ComponentManager componentManager;

	@SingletonDependency
	ConsoleMetaManager consoleMetaManager;

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
	String beanName;

	String objectTitle;
	Optional <String> defaultFileName;

	Cryptor cryptor;

	Boolean hasListChildren;
	Boolean hasObjectChildren;

	List <String> listContextTypeNames;
	List <String> objectContextTypeNames;

	// build

	@BuildMethod
	public
	void build (
			@NonNull Builder builder) {

		setDefaults ();

		buildContextTypes ();

		buildSimpleContexts ();
		buildSimpleTabs ();

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
				resolvedContextLink);

		}

		ConsoleContextBuilderContainer <ObjectType> listContainer =
			new ConsoleContextBuilderContainerImplementation <ObjectType> ()

			.taskLogger (
				container.taskLogger ())

			.consoleHelper (
				consoleHelper)

			.structuralName (
				structuralName)

			.extensionPointName (
				name + ":list")

			.pathPrefix (
				name)

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
			spec.listChildren (),
			consoleModule,
			MissingBuilderBehaviour.error);

		ConsoleContextBuilderContainer <ObjectType> objectContainer =
			new ConsoleContextBuilderContainerImplementation <ObjectType> ()

			.taskLogger (
				container.taskLogger ())

			.consoleHelper (
				consoleHelper)

			.structuralName (
				structuralName)

			.extensionPointName (
				name + ":object")

			.pathPrefix (
				name)

			.newBeanNamePrefix (
				beanName)

			.existingBeanNamePrefix (
				beanName)

			.tabLocation (
				"end")

			.friendlyName (
				camelToSpaces (beanName));

		builder.descend (
			objectContainer,
			spec.objectChildren (),
			consoleModule,
			MissingBuilderBehaviour.error);

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

	void buildSimpleTabs () {

		consoleModule.addContextTab (
			container.taskLogger (),
			"link",

			contextTabProvider.get ()

				.name (
					"link:" + name)

				.defaultLabel (
					"Object title"),

			ImmutableList.<String> of ());

		consoleModule.addContextTab (
			container.taskLogger (),
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

			Collections.<String> emptyList ());

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
			ResolvedConsoleContextLink resolvedConsoleContextLink) {

		consoleModule.addContextTab (
			container.taskLogger (),
			resolvedConsoleContextLink.tabLocation (),

			contextTabProvider.get ()

				.name (
					resolvedConsoleContextLink.tabName ())

				.defaultLabel (
					resolvedConsoleContextLink.tabLabel ())

				.privKeys (
					resolvedConsoleContextLink.tabPrivKey ())

				.localFile (
					"type:" + name + ":list"),

			resolvedConsoleContextLink.tabContextTypeNames ());

	}

	// defaults

	void setDefaults () {

		consoleHelper =
			genericCastUnchecked (
				objectManager.findConsoleHelperRequired (
					spec.objectName ()));

		name =
			spec.name ();

		structuralName =
			name;

		beanName =
			ifNull (
				spec.beanName (),
				name);

		if (beanName.contains ("_")) {

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
					log,
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
