package wbs.console.module;

import static wbs.framework.utils.etc.Misc.contains;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Provider;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.context.ConsoleContext;
import wbs.console.context.ConsoleContextType;
import wbs.console.forms.FormFieldSet;
import wbs.console.supervisor.SupervisorConfig;
import wbs.console.tab.ConsoleContextTab;
import wbs.console.tab.ContextTabPlacement;
import wbs.console.tab.TabContextResponder;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.annotations.PrototypeDependency;
import wbs.framework.application.annotations.SingletonDependency;
import wbs.framework.application.context.ApplicationContext;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.web.PathHandler;
import wbs.framework.web.Responder;
import wbs.framework.web.WebFile;

@Accessors (fluent = true)
@DataClass ("console-module")
@PrototypeComponent ("consoleModuleImpl")
public
class ConsoleModuleImplementation
	implements ConsoleModule {

	// singleton dependencies

	@SingletonDependency
	ApplicationContext applicationContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <ConsoleContextTab> contextTabProvider;

	@PrototypeDependency
	Provider <TabContextResponder> tabContextResponderProvider;

	// properties

	@DataChildren
	@Getter @Setter
	List<ConsoleContextType> contextTypes =
		new ArrayList<ConsoleContextType> ();

	@DataChildren
	@Getter @Setter
	List<ConsoleContext> contexts =
		new ArrayList<ConsoleContext> ();

	@DataChildren
	@Getter @Setter
	List<ConsoleContextTab> tabs =
		new ArrayList<ConsoleContextTab> ();

	Set<String> tabNames =
		new HashSet<String> ();

	@DataChildren
	@Getter @Setter
	Map<String,List<ContextTabPlacement>> tabPlacementsByContextType =
		new LinkedHashMap<String,List<ContextTabPlacement>> ();

	@DataChildren
	@Getter @Setter
	Map<String,WebFile> contextFiles =
		new LinkedHashMap<String,WebFile> ();

	@DataChildren
	@Getter @Setter
	Map<String,List<String>> contextFilesByContextType =
		new LinkedHashMap<String,List<String>> ();

	@DataChildren
	@Getter @Setter
	Map<String,Provider<Responder>> responders =
		new LinkedHashMap<String,Provider<Responder>> ();

	@DataChildren
	@Getter @Setter
	Map<String,PathHandler> paths =
		new LinkedHashMap<String,PathHandler> ();

	@DataChildren
	@Getter @Setter
	Map<String,WebFile> files =
		new LinkedHashMap<String,WebFile> ();

	@DataChildren
	@Getter @Setter
	Map<String,FormFieldSet> formFieldSets =
		new LinkedHashMap<String,FormFieldSet> ();

	@DataChildren
	@Getter @Setter
	Map<String,SupervisorConfig> supervisorConfigs =
		new LinkedHashMap<String,SupervisorConfig> ();

	// utils

	public
	Provider<Responder> beanResponder (
			@NonNull String name) {

		return applicationContext.getComponentProviderRequired (
			name,
			Responder.class);

	}

	// builder tools

	public
	void addContextTab (
			@NonNull String tabLocation,
			@NonNull ConsoleContextTab tab,
			@NonNull List<String> contextTypes) {

		if (tab.name ().isEmpty ())
			throw new RuntimeException ();

		if (
			contains (
				tabNames,
				tab.name ())
		) {

			throw new RuntimeException ();

		}

		tabs.add (
			tab);

		tabNames.add (
			tab.name ());

		for (
			String contextTypeName
				: contextTypes
		) {

			List<ContextTabPlacement> tabPlacements =
				tabPlacementsByContextType.get (
					contextTypeName);

			if (tabPlacements == null) {

				tabPlacements =
					new ArrayList<ContextTabPlacement> ();

				tabPlacementsByContextType.put (
					contextTypeName,
					tabPlacements);

			}

			tabPlacements.add (
				new ContextTabPlacement ()

				.tabLocation (
					tabLocation)

				.tabName (
					tab.name ()));

		}

	}

	public
	void addContextFile (
			@NonNull String name,
			@NonNull WebFile file,
			@NonNull List<String> contextTypeNames) {

		if (contextFiles.containsKey (
				name)) {

			throw new RuntimeException (
				stringFormat (
					"Duplicated context file name: %s",
					name));

		}

		contextFiles.put (
			name,
			file);

		for (String contextType
				: contextTypeNames) {

			List<String> contextTypeFiles =
				contextFilesByContextType.get (contextType);

			if (contextTypeFiles == null) {

				contextTypeFiles =
					new ArrayList<String> ();

				contextFilesByContextType.put (
					contextType,
					contextTypeFiles);

			}

			contextTypeFiles.add (name);

		}

	}

	public
	void addResponder (
			@NonNull String name,
			@NonNull Provider<Responder> responder) {

		responders.put (
			name,
			responder);

	}

	public
	void addContextType (
			@NonNull ConsoleContextType contextType) {

		contextTypes.add (
			contextType);

	}

	public
	void addContext (
			@NonNull ConsoleContext consoleContext) {

		contexts.add (
			consoleContext);

	}

	public
	void addFile (
			@NonNull String path,
			@NonNull WebFile file) {

		if (files.containsKey (
			path)) {

			throw new RuntimeException (
				stringFormat (
					"Duplicated file path: %s",
					path));

		}

		files.put (
			path,
			file);

	}

	public
	void addPath (
			@NonNull String path,
			@NonNull PathHandler pathHandler) {

		paths.put (
			path,
			pathHandler);

	}

	public
	void addTabLocation (
			@NonNull String insertLocationName,
			@NonNull String newLocationName,
			@NonNull List<String> contextTypeNames) {

		for (String contextTypeName
				: contextTypeNames) {

			List<ContextTabPlacement> tabPlacementsForContextType =
				tabPlacementsByContextType.get (
					contextTypeName);

			if (tabPlacementsForContextType == null) {

				tabPlacementsForContextType =
					new ArrayList<ContextTabPlacement> ();

				tabPlacementsByContextType.put (
					contextTypeName,
					tabPlacementsForContextType);

			}

			tabPlacementsForContextType.add (
				new ContextTabPlacement ()
					.tabLocation (
						insertLocationName)
					.tabName (
						"+" + newLocationName));

		}

	}

	public
	void addFormFieldSet (
			@NonNull String name,
			@NonNull FormFieldSet formFieldSet) {

		if (formFieldSets.containsKey (
				name)) {

			throw new RuntimeException (
				stringFormat (
					"Duplicated form field set name: %s",
					name));

		}

		formFieldSets.put (
			name,
			formFieldSet);

	}

	public
	void addSupervisorConfig (
			SupervisorConfig supervisorConfig) {

		if (
			supervisorConfigs.containsKey (
				supervisorConfig.name ())
		) {

			throw new RuntimeException ();

		}

		supervisorConfigs.put (
			supervisorConfig.name (),
			supervisorConfig);

	}

}
