package wbs.platform.console.object;

import static wbs.framework.utils.etc.Misc.camelToSpaces;
import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.maybeList;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.context.ApplicationContext;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.platform.console.annotations.ConsoleModuleBuilderHandler;
import wbs.platform.console.context.ConsoleContextBuilderContainer;
import wbs.platform.console.context.ConsoleContextBuilderContainerImpl;
import wbs.platform.console.context.ConsoleContextType;
import wbs.platform.console.context.SimpleConsoleContext;
import wbs.platform.console.helper.ConsoleHelper;
import wbs.platform.console.helper.ConsoleHelperRegistry;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.console.metamodule.ConsoleMetaManager;
import wbs.platform.console.metamodule.ResolvedConsoleContextLink;
import wbs.platform.console.module.ConsoleModuleImpl;
import wbs.platform.console.request.Cryptor;
import wbs.platform.console.spec.ConsoleSimpleBuilderContainer;
import wbs.platform.console.tab.ConsoleContextTab;

import com.google.common.collect.ImmutableList;

@PrototypeComponent ("objectContextBuilder")
@ConsoleModuleBuilderHandler
public
class ObjectContextBuilder {

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
	Provider<BabyObjectContext> babyObjectContext;

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
	ConsoleSimpleBuilderContainer container;

	@BuilderSource
	ObjectContextSpec spec;

	@BuilderTarget
	ConsoleModuleImpl consoleModule;

	// state

	ConsoleHelper<?> consoleHelper;

	String name;
	String structuralName;
	String beanName;

	String objectTitle;

	Cryptor cryptor;

	Boolean hasListChildren;
	Boolean hasObjectChildren;

	List<String> listContextTypeNames;
	List<String> objectContextTypeNames;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		setDefaults ();

		buildContextTypes ();

		List<ResolvedConsoleContextLink> resolvedContextLinks =
			consoleMetaManager.resolveContextLink (
				name);

		buildSimpleContexts ();
		buildSimpleTabs ();

		for (ResolvedConsoleContextLink resolvedContextLink
				: resolvedContextLinks) {

			buildResolvedContexts (
				resolvedContextLink);

			buildResolvedTabs (
				resolvedContextLink);

		}

		buildLinkTabs ();

		ConsoleContextBuilderContainer listContainer =
			new ConsoleContextBuilderContainerImpl ()

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
			consoleModule);

		ConsoleContextBuilderContainer objectContainer =
			new ConsoleContextBuilderContainerImpl ()

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
			consoleModule);

	}

	void buildContextTypes () {

		consoleModule.addContextType (
			contextType.get ()

			.name (
				name + "s"));

		consoleModule.addContextType (
			contextType.get ()

			.name (
				name + "+"));

		consoleModule.addContextType (
			contextType.get ()

			.name (
				name));

	}

	void buildSimpleContexts () {

		consoleModule.addContext (
			simpleContext.get ()

			.name (
				name + "s")

			.typeName (
				name + "s")

			.pathPrefix (
				"/" + name + "s")

			.global (
				true)

			.title (capitalise (
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

	}

	void buildResolvedContexts (
			ResolvedConsoleContextLink resolvedContextLink) {

		for (String parentContextName
				: resolvedContextLink.parentContextNames ()) {

			String resolvedContextName =
				stringFormat (
					"%s.%s",
					parentContextName,
					resolvedContextLink.localName ());

			consoleModule.addContext (
				babyObjectContext.get ()

				.name (
					resolvedContextName + "s")

				.typeName (
					name + "s")

				.pathPrefix (
					"/" + resolvedContextName + "s")

				.global (
					true)

				.title (capitalise (
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
					"/" + resolvedContextName)

				.global (
					true)

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
					resolvedConsoleContextLink.tabFile ()),

			resolvedConsoleContextLink.tabContextTypeNames ());

	}

	public
	void buildLinkTabs () {

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

	// defaults

	void setDefaults () {

		consoleHelper =
			consoleHelperRegistry.findByObjectName (
				spec.objectName ());

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

				.addAll (maybeList (
					hasListChildren,
					name + "s"))

				.addAll (maybeList (
					hasObjectChildren,
					name + "+"))

				.build ();

		objectContextTypeNames =
			ImmutableList.<String>builder ()

				.addAll (maybeList (
					hasListChildren,
					name + "+"))

				.addAll (maybeList (
					hasObjectChildren,
					name))

				.build ();

	}

}
