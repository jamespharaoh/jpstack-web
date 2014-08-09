package wbs.platform.supervisor;

import static wbs.framework.utils.etc.Misc.camelToSpaces;
import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.context.ApplicationContext;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.platform.console.annotations.ConsoleModuleBuilderHandler;
import wbs.platform.console.context.ConsoleContextBuilderContainer;
import wbs.platform.console.context.ResolvedConsoleContextExtensionPoint;
import wbs.platform.console.helper.ConsoleHelper;
import wbs.platform.console.metamodule.ConsoleMetaManager;
import wbs.platform.console.module.ConsoleModuleImpl;
import wbs.platform.console.part.PagePart;
import wbs.platform.console.responder.ConsoleFile;
import wbs.platform.console.tab.ConsoleContextTab;
import wbs.platform.console.tab.TabContextResponder;
import wbs.platform.reporting.console.StatsAggregator;
import wbs.platform.reporting.console.StatsFormatter;
import wbs.platform.reporting.console.StatsGrouper;
import wbs.platform.reporting.console.StatsProvider;
import wbs.platform.reporting.console.StatsResolver;

@Accessors (fluent = true)
@PrototypeComponent ("supervisorPageBuilder")
@ConsoleModuleBuilderHandler
public
class SupervisorPageBuilder {

	// dependencies

	@Inject
	ApplicationContext applicationContext;

	@Inject
	ConsoleMetaManager consoleMetaManager;

	// prototype dependencies

	@Inject
	Provider<ConsoleFile> consoleFile;

	@Inject
	Provider<ConsoleContextTab> contextTab;

	@Inject
	Provider<SupervisorPart> supervisorPart;

	@Inject
	Provider<TabContextResponder> tabContextResponder;

	// builder

	@BuilderParent
	ConsoleContextBuilderContainer container;

	@BuilderSource
	SupervisorPageSpec spec;

	@BuilderTarget
	ConsoleModuleImpl consoleModule;

	// state

	ConsoleHelper<?> consoleHelper;

	String name;
	String tabName;
	String tabLocation;
	String tabLabel;
	String fileName;
	String responderName;
	String title;

	Provider<PagePart> pagePartFactory;

	@Getter @Setter
	Map<String,StatsProvider> statsProvidersByName =
		new LinkedHashMap<String,StatsProvider> ();

	@Getter @Setter
	Map<String,StatsAggregator> statsAggregatorsByName =
		new LinkedHashMap<String,StatsAggregator> ();

	@Getter @Setter
	Map<String,StatsFormatter> statsFormattersByName =
		new LinkedHashMap<String,StatsFormatter> ();

	@Getter @Setter
	Map<String,StatsGrouper> statsGroupersByName =
		new LinkedHashMap<String,StatsGrouper> ();

	@Getter @Setter
	Map<String,StatsResolver> statsResolversByName =
		new LinkedHashMap<String,StatsResolver> ();

	@Getter @Setter
	List<Provider<PagePart>> pagePartFactories =
		new ArrayList<Provider<PagePart>> ();

	// build

	@BuildMethod
	public
	void buildConsoleModule (
			Builder builder) {

		setDefaults ();

		builder.descend (
			spec,
			spec.builders (),
			this);

		for (ResolvedConsoleContextExtensionPoint resolvedExtensionPoint
				: consoleMetaManager.resolveExtensionPoint (
					container.extensionPointName ())) {

			buildContextTab (
				resolvedExtensionPoint);

			buildContextFile (
				resolvedExtensionPoint);

		}

		buildPagePartFactory ();
		buildResponder ();

	}

	void buildContextTab (
			ResolvedConsoleContextExtensionPoint resolvedExtensionPoint) {

		consoleModule.addContextTab (
			container.tabLocation (),
			contextTab.get ()
				.name (tabName)
				.defaultLabel (tabLabel)
				.localFile (fileName),
			resolvedExtensionPoint.contextTypeNames ());

	}

	void buildContextFile (
			ResolvedConsoleContextExtensionPoint resolvedExtensionPoint) {

		consoleModule.addContextFile (
			fileName,
			consoleFile.get ()
				.getResponderName (responderName),
			resolvedExtensionPoint.contextTypeNames ());

	}

	void buildPagePartFactory () {

		pagePartFactory =
			new Provider<PagePart> () {

			@Override
			public
			PagePart get () {

				return supervisorPart.get ()
					.supervisorPageSpec (spec)
					.pagePartFactories (pagePartFactories);

			}

		};

	}

	void buildResponder () {

		consoleModule.addResponder (
			responderName,
			tabContextResponder.get ()
				.tab (tabName)
				.title (title)
				.pagePartFactory (pagePartFactory));

	}

	void setDefaults () {

		consoleHelper =
			container.consoleHelper ();

		name =
			ifNull (
				spec.name (),
				"supervisor");

		tabName =
			ifNull (
				spec.tabName (),
				stringFormat (
					"%s.%s",
					container.pathPrefix (),
					name));

		tabLocation =
			container.tabLocation ();

		tabLabel =
			ifNull (
				spec.tabLabel (),
				capitalise (camelToSpaces (name)));

		fileName =
			ifNull (
				spec.fileName (),
				stringFormat (
					"%s.%s",
					container.pathPrefix (),
					name));

		responderName =
			ifNull (
				spec.responderName (),
				stringFormat (
					"%s%sResponder",
					container.newBeanNamePrefix (),
					capitalise (name)));

		title =
			ifNull (
				spec.title (),
				"Supervisor");

	}

}
