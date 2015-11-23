package wbs.console.object;

import static wbs.framework.utils.etc.Misc.camelToSpaces;
import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.joinWithoutSeparator;
import static wbs.framework.utils.etc.Misc.maybeList;
import static wbs.framework.utils.etc.Misc.naivePluralise;
import static wbs.framework.utils.etc.Misc.startsWith;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.NonNull;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.context.ConsoleContextBuilderContainer;
import wbs.console.context.ConsoleContextBuilderContainerImplementation;
import wbs.console.context.ConsoleContextType;
import wbs.console.context.SimpleConsoleContext;
import wbs.console.helper.ConsoleHelper;
import wbs.console.helper.ConsoleHelperRegistry;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.module.ConsoleMetaManager;
import wbs.console.module.ConsoleModuleImplementation;
import wbs.console.module.ResolvedConsoleContextLink;
import wbs.console.module.SimpleConsoleBuilderContainer;
import wbs.console.request.Cryptor;
import wbs.console.tab.ConsoleContextTab;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.context.ApplicationContext;
import wbs.framework.builder.Builder;
import wbs.framework.builder.Builder.MissingBuilderBehaviour;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.record.Record;

@PrototypeComponent ("objectContextBuilder")
@ConsoleModuleBuilderHandler
public
class ObjectContextBuilder<
	ObjectType extends Record<ObjectType>
> {

	// dependencies

	@Inject
	ApplicationContext applicationContext;

	@Inject
	ConsoleHelperRegistry consoleHelperRegistry;

	@Inject
	ConsoleMetaManager consoleMetaManager;

	@Inject
	ConsoleObjectManager objectManager;

	// prototype dependencies

	@Inject
	Provider<SimpleConsoleContext> simpleConsoleContextProvider;

	@Inject
	Provider<ConsoleContextTab> contextTab;

	@Inject
	Provider<ConsoleContextType> contextType;

	@Inject
	Provider<ObjectContext> objectContext;

	@Inject
	Provider<SimpleConsoleContext> simpleContext;

	// builder

	@BuilderParent
	SimpleConsoleBuilderContainer container;

	@BuilderSource
	ObjectContextSpec spec;

	@BuilderTarget
	ConsoleModuleImplementation consoleModule;

	// state

	ConsoleHelper<ObjectType> consoleHelper;

	String name;
	String structuralName;
	String beanName;

	String objectTitle;
	Optional<String> defaultFileName;

	Cryptor cryptor;

	Boolean hasListChildren;
	Boolean hasObjectChildren;

	List<String> listContextTypeNames;
	List<String> objectContextTypeNames;

	// build

	@BuildMethod
	public
	void build (
			@NonNull Builder builder) {

		setDefaults ();

		buildContextTypes ();

		buildSimpleContexts ();
		buildSimpleTabs ();

		List<ResolvedConsoleContextLink> resolvedContextLinks =
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

		ConsoleContextBuilderContainer<ObjectType> listContainer =
			new ConsoleContextBuilderContainerImplementation<ObjectType> ()

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
				camelToSpaces (beanName));

		builder.descend (
			listContainer,
			spec.listChildren (),
			consoleModule,
			MissingBuilderBehaviour.error);

		ConsoleContextBuilderContainer<ObjectType> objectContainer =
			new ConsoleContextBuilderContainerImplementation<ObjectType> ()

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
			contextType.get ()

			.name (
				naivePluralise (
					name))

			.defaultFileName (
				defaultFileName.orNull ()));

		consoleModule.addContextType (
			contextType.get ()

			.name (
				name + "+")

			.defaultFileName (
				defaultFileName.orNull ()));

		consoleModule.addContextType (
			contextType.get ()

			.name (
				name)

			.defaultFileName (
				defaultFileName.orNull ()));

	}

	void buildSimpleContexts () {

		consoleModule.addContext (
			simpleContext.get ()

			.name (
				naivePluralise (
					name))

			.typeName (
				naivePluralise (
					name))

			.pathPrefix (
				"/" + naivePluralise (
					name))

			.global (
				true)

			.title (
				capitalise (
					consoleHelper.shortNamePlural ())));

		consoleModule.addContext (
			objectContext.get ()

			.name (
				name)

			.typeName (
				name + "+")

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
			objectContext.get ()

			.name (
				"link:" + name)

			.typeName (
				name)

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
			"link",

			contextTab.get ()

				.name (
					"link:" + name)

				.defaultLabel (
					"Object title"),

			ImmutableList.<String>of ());

		consoleModule.addContextTab (
			"end",

			contextTab.get ()

				.name (
					stringFormat (
						"%s",
						name,
						":link"))

				.defaultLabel (
					capitalise (
						consoleHelper.friendlyName ())),

			Collections.<String>emptyList ());

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
				startsWith (
					resolvedContextName,
					"link:");

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
					naivePluralise (
						name))

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
				objectContext.get ()

				.name (
					resolvedContextName)

				.typeName (
					name + "+")

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
			resolvedConsoleContextLink.tabLocation (),

			contextTab.get ()

				.name (
					resolvedConsoleContextLink.tabName ())

				.defaultLabel (
					resolvedConsoleContextLink.tabLabel ())

				.privKeys (
					resolvedConsoleContextLink.tabPrivKey ())

				.localFile (
					"type:" + naivePluralise (
						name)),

			resolvedConsoleContextLink.tabContextTypeNames ());

	}

	// defaults

	void setDefaults () {

		@SuppressWarnings ("unchecked")
		ConsoleHelper<ObjectType> consoleHelperTemp =
			(ConsoleHelper<ObjectType>)
			consoleHelperRegistry.findByObjectName (
				spec.objectName ());

		consoleHelper =
			consoleHelperTemp;

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
				? applicationContext.getBean (
					spec.cryptorBeanName (),
					Cryptor.class)
				: null;

		hasListChildren =
			! spec.listChildren ().isEmpty ();

		hasObjectChildren =
			! spec.objectChildren ().isEmpty ();

		listContextTypeNames =
			ImmutableList.<String>builder ()

				.addAll (
					maybeList (
						hasListChildren,
						naivePluralise (
							name)))

				.addAll (
					maybeList (
						hasObjectChildren,
						naivePluralise (
							name)))

				.build ();

		objectContextTypeNames =
			ImmutableList.<String>builder ()

				.addAll (
					maybeList (
						hasListChildren,
						name + "+"))

				.addAll (
					maybeList (
						hasObjectChildren,
						name))

				.build ();

	}

}
