package wbs.console.module;

import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.io.FileUtils.deleteDirectory;
import static wbs.utils.io.FileUtils.forceMkdir;
import static wbs.utils.string.StringUtils.camelToHyphen;
import static wbs.utils.string.StringUtils.joinWithCommaAndSpace;
import static wbs.utils.string.StringUtils.joinWithoutSeparator;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringNotEqualSafe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.tuple.Pair;

import wbs.console.context.ConsoleContext;
import wbs.console.context.ConsoleContext.PathSupply;
import wbs.console.context.ConsoleContextStuff;
import wbs.console.context.ConsoleContextType;
import wbs.console.helper.core.ConsoleHelper;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.helper.provider.ConsoleHelperProvider;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.supervisor.SupervisorConfig;
import wbs.console.tab.ConsoleContextTab;
import wbs.console.tab.ContextTabPlacement;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.data.tools.DataToXml;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.web.exceptions.ExternalRedirectException;
import wbs.web.exceptions.HttpNotFoundException;
import wbs.web.file.WebFile;
import wbs.web.pathhandler.PathHandler;
import wbs.web.responder.WebModule;

@Accessors (fluent = true)
@SingletonComponent ("consoleManager")
public
class ConsoleManagerImplementation
	implements
		ConsoleManager,
		WebModule {

	// singleton dependencies

	@SingletonDependency
	ConsoleMetaManager consoleMetaManager;

	@SingletonDependency
	@Getter
	Map <String, ConsoleModule> consoleModules;

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <ConsoleContextStuff> contextStuffProvider;

	// properties

	@Getter
	Map <String, WebFile> files =
		emptyMap ();

	@Getter
	Map <String, PathHandler> paths;

	// state

	Map <String, ConsoleContextType> contextTypesByName =
		new HashMap<> ();

	Map <String, ConsoleContext> consoleContextsByName =
		new HashMap<> ();

	List <ConsoleContext> consoleContexts =
		new ArrayList<> ();

	Map <String, ConsoleContextTab> contextTabs =
		new HashMap<> ();

	Map <String, WebFile> contextFiles =
		new HashMap<> ();

	Map <Pair <String, String>, List <ConsoleContext>> contextsByParentAndType =
		new HashMap<> ();

	Map <String, List <ConsoleContext>> contextsWithoutParentByType =
		new HashMap<> ();

	Map <String, SupervisorConfig> supervisorConfigs =
		new HashMap<> ();

	// implementation

	@Override
	public
	ConsoleContext context (
			@NonNull String contextName,
			boolean required) {

		ConsoleContext context =
			consoleContextsByName.get (
				contextName);

		if (context == null && required) {

			throw new RuntimeException (
				stringFormat (
					"No such context: %s",
					contextName));

		}

		return context;

	}

	@Override
	public
	ConsoleContextTab tab (
			@NonNull String tabName,
			boolean required) {

		ConsoleContextTab tab =
			contextTabs.get (
				tabName);

		if (tab == null && required) {

			throw new RuntimeException (
				stringFormat (
					"No such tab: %s",
					tabName));

		}

		return tab;

	}

	public
	WebFile contextFile (
			@NonNull String name) {

		return contextFiles.get (
			name);

	}

	/**
	 * Queries the application context for console modules and sets up their
	 * contexts, tabs and files automatically.
	 */
	@NormalLifecycleSetup
	public
	void init (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"init");

		) {

			dumpData (
				taskLogger);

			// find responders

			collectContextTypes (
				taskLogger);

			collectContextTabs (
				taskLogger);

			addTabsToContextTypes (
				taskLogger);

			collectContextFiles (
				taskLogger);

			collectContextTypeFiles (
				taskLogger);

			resolveContextTabs (
				taskLogger);

			collectConsoleContexts (
				taskLogger);

			collectContextsByParentType (
				taskLogger);

			collectSupervisorConfigs (
				taskLogger);

			initPaths (
				taskLogger);

			taskLogger.makeException ();

		}

	}

	int collectContextTypes (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"collectContextTypes");

		) {

			int errors = 0;

			Map <String, String> beanNamesByContextTypeName =
				new HashMap<> ();

			for (
				Map.Entry <String, ConsoleModule> ent
					: consoleModules.entrySet ()
			) {

				String beanName =
					ent.getKey ();

				ConsoleModule consoleModule =
					ent.getValue ();

				for (
					ConsoleContextType contextType
						: consoleModule.contextTypes ()
				) {

					if (contextTypesByName.containsKey (contextType.name ())) {

						taskLogger.errorFormat (
							"Duplicated context type %s in %s and %s",
							contextType.name (),
							beanName,
							beanNamesByContextTypeName.get (
								contextType.name ()));

						errors ++;

						continue;

					}

					beanNamesByContextTypeName.put (
						contextType.name (),
						beanName);

					contextTypesByName.put (
						contextType.name (),
						contextType);

					taskLogger.debugFormat (
						"Adding context type %s from %s",
						contextType.name (),
						beanName);

				}

			}

			return errors;

		}

	}

	int collectContextTabs (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"collectContextTabs");

		) {

			int errors = 0;

			for (
				Map.Entry<String,ConsoleModule> consoleModuleEntry
					: consoleModules.entrySet ()
			) {

				String consoleModuleName =
					consoleModuleEntry.getKey ();

				ConsoleModule consoleModule =
					consoleModuleEntry.getValue ();

				for (
					ConsoleContextTab contextTab
						: consoleModule.tabs ()
				) {

					if (contextTabs.containsKey (
							contextTab.name ())) {

						taskLogger.errorFormat (
							"Duplicated tab %s in %s",
							contextTab.name (),
							consoleModuleName);

						errors ++;

						continue;

					}

					contextTabs.put (
						contextTab.name (),
						contextTab);

					taskLogger.debugFormat (
						"Adding tab %s from %s",
						contextTab.name (),
						consoleModuleName);

				}

			}

			return errors;

		}

	}

	int addTabsToContextTypes (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"addTabsToContextTypes");

		) {

			int errors = 0;

			for (
				Map.Entry<String,ConsoleModule> entry
					: consoleModules.entrySet ()
			) {

				String consoleModuleName =
					entry.getKey ();

				ConsoleModule consoleModule =
					entry.getValue ();

				for (
					Map.Entry<String,List<ContextTabPlacement>> tabPlacementsEntry
						: consoleModule
							.tabPlacementsByContextType ()
							.entrySet ()
				) {

					String contextTypeName =
						tabPlacementsEntry.getKey ();

					List<ContextTabPlacement> tabPlacements =
						tabPlacementsEntry.getValue ();

					ConsoleContextType contextType =
						contextTypesByName.get (
							contextTypeName);

					if (contextType == null) {

						List<String> contextTabNames =
							new ArrayList<String> ();

						for (
							ContextTabPlacement tabPlacement
								: tabPlacements
						) {

							contextTabNames.add (
								stringFormat (
									"%s:%s",
									tabPlacement.tabLocation (),
									tabPlacement.tabName ()));

						}

						taskLogger.errorFormat (
							"Unknown context type %s ",
							contextTypeName,
							"referenced from tabs %s ",
							joinWithCommaAndSpace (
								contextTabNames),
							"in %s",
							consoleModuleName);

						errors ++;

						continue;

					}

					for (
						ContextTabPlacement tabPlacement
							: tabPlacements
					) {

						String contextTabName =
							tabPlacement.tabName ();

						if (contextTabName.charAt (0) != '+') {

							ConsoleContextTab contextTab =
								contextTabs.get (contextTabName);

							if (contextTab == null) {

								taskLogger.errorFormat (
									"Unknown tab %s referenced from %s",
									contextTabName,
									consoleModuleName);

								errors ++;

								continue;

							}

						}

						contextType.tabPlacements ().add (
							tabPlacement);

					}

				}

			}

			return errors;

		}

	}

	int collectContextFiles (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"collectContextFiles");

		) {

			int errors = 0;

			for (
				Map.Entry<String,ConsoleModule> consoleModuleEntry
					: consoleModules.entrySet ()
			) {

				String consoleModuleName =
					consoleModuleEntry.getKey ();

				ConsoleModule consoleModule =
					consoleModuleEntry.getValue ();

				for (
					Map.Entry<String,? extends WebFile> contextFileEntry
						: consoleModule.contextFiles ().entrySet ()
				) {

					String contextFileName =
						contextFileEntry.getKey ();

					WebFile contextFile =
						contextFileEntry.getValue ();

					if (contextFiles.containsKey (
							contextFileName)) {

						taskLogger.errorFormat (
							"Duplicated context file: %s",
							contextFileName);

						errors ++;

						continue;

					}

					contextFiles.put (
						contextFileName,
						contextFile);

					taskLogger.debugFormat (
						"Adding file %s from %s",
						contextFileName,
						consoleModuleName);

				}

			}

			return errors;

		}

	}

	int collectContextTypeFiles (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"collectContextTypeFiles");

		) {

			int errors = 0;

			for (
				Map.Entry<String,ConsoleModule> consoleModuleEntry
					: consoleModules.entrySet ()
			) {

				String consoleModuleName =
					consoleModuleEntry.getKey ();

				ConsoleModule consoleModule =
					consoleModuleEntry.getValue ();

				for (
					Map.Entry<String,? extends Collection<String>>
					contextFilesByContextTypeEntry
						: consoleModule
							.contextFilesByContextType ()
							.entrySet ()
				) {

					String contextTypeName =
						contextFilesByContextTypeEntry.getKey ();

					Collection<String> contextFileNames =
						contextFilesByContextTypeEntry.getValue ();

					ConsoleContextType contextType =
						contextTypesByName.get (contextTypeName);

					if (contextType == null) {

						taskLogger.errorFormat (
							"Unknown context type %s referenced from files in ",
							contextTypeName,
							"%s",
							consoleModuleName);

						errors ++;

						continue;

					}

					Map<String,WebFile> typeContextFiles =
						contextType.files ();

					for (
						String contextFileName
							: contextFileNames
					) {

						WebFile contextFile =
							contextFiles.get (contextFileName);

						if (contextFile == null) {

							taskLogger.errorFormat (
								"Unknown file %s references from context type ",
								contextFileName,
								"%s",
								contextTypeName);

							errors ++;

							continue;

						}

						typeContextFiles.put (
							contextFileName,
							contextFile);

					}

				}

			}

			return errors;

		}

	}

	int resolveContextTabs (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"resolveContextTabs");

		) {

			int errors = 0;

			for (
				ConsoleContextType contextType
					: contextTypesByName.values ()
			) {

				try {

					contextType.resolveTabSpecs (
						taskLogger,
						contextTabs);

				} catch (Exception exception) {

					taskLogger.errorFormatException (
						exception,
						"Error resolving tab specs for %s",
						contextType.name ());

					errors ++;

					taskLogger.noticeFormat (
						"Dumping context type tab specification for %s",
						contextType.name ());

					for (
						ContextTabPlacement tabPlacement
							: contextType.tabPlacements ()
					) {

						taskLogger.noticeFormat (
							"%s => %s",
							tabPlacement.tabLocation (),
							tabPlacement.tabName ());

					}

				}

			}

			return errors;

		}

	}

	int collectConsoleContexts (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"collectConsoleContexts");

		) {

			int errors = 0;

			for (
				Map.Entry<String,ConsoleModule> consoleModuleEntry
					: consoleModules.entrySet ()
			) {

				String consoleModuleName =
					consoleModuleEntry.getKey ();

				ConsoleModule consoleModule =
					consoleModuleEntry.getValue ();

				for (
					ConsoleContext consoleContext
						: consoleModule.contexts ()
				) {

					if (
						consoleContextsByName.containsKey (
							consoleContext.name ())
					) {

						taskLogger.errorFormat (
							"Duplicated context name: %s",
							consoleContext.name ());

						errors ++;

						continue;

					}

					consoleContextsByName.put (
						consoleContext.name (),
						consoleContext);

					ConsoleContextType contextType =
						contextTypesByName.get (
							consoleContext.typeName ());

					if (contextType == null) {

						taskLogger.errorFormat (
							"Unknown context type %s ",
							consoleContext.typeName (),
							"referenced from context %s ",
							consoleContext.name (),
							"in %s",
							consoleModuleName);

						errors ++;

						continue;

					}

					consoleContext

						.contextType (
							contextType)

						.files (
							contextType.files ())

						.contextTabs (
							contextType.tabs ());

					taskLogger.debugFormat (
						"Adding context %s from %s",
						consoleContext.name (),
						consoleModuleName);

				}

			}

			consoleContexts.addAll (
				consoleContextsByName.values ());

			Collections.sort (
				consoleContexts);

			return errors;

		}

	}

	void collectContextsByParentType (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"collectContextsByParentType");

		) {

			for (
				ConsoleContext context
					: consoleContexts
			) {

				if (context.parentContextName () != null) {

					Pair <String, String> key =
						Pair.of (
							context.parentContextName (),
							context.typeName ());

					List<ConsoleContext> contextsForParentAndType =
						contextsByParentAndType.get (key);

					if (contextsForParentAndType == null) {

						contextsForParentAndType =
							new ArrayList<> ();

						contextsByParentAndType.put (
							key,
							contextsForParentAndType);

					}

					contextsForParentAndType.add (
						context);

				} else {

					List <ConsoleContext> contextsForType =
						contextsWithoutParentByType.get (
							context.typeName ());

					if (contextsForType == null) {

						contextsForType =
							new ArrayList<> ();

						contextsWithoutParentByType.put (
							context.typeName (),
							contextsForType);

					}

					contextsForType.add (
						context);

				}

			}

		}

	}

	void collectSupervisorConfigs (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"collectSupervisorConfigs");

		) {

			for (
				Map.Entry<String,ConsoleModule> consoleModuleEntry
					: consoleModules.entrySet ()
			) {

				String consoleModuleName =
					consoleModuleEntry.getKey ();

				ConsoleModule consoleModule =
					consoleModuleEntry.getValue ();

				for (
					Map.Entry<String,SupervisorConfig> entry
						: consoleModule.supervisorConfigs ().entrySet ()
				) {

					String supervisorConfigName =
						entry.getKey ();

					SupervisorConfig supervisorConfig =
						entry.getValue ();

					if (
						supervisorConfigs.containsKey (
							supervisorConfigName)
					) {

						taskLogger.errorFormat (
							"Duplicated supervisor config name %s ",
							supervisorConfigName,
							" in console module %s",
							consoleModuleName);

					}

					supervisorConfigs.put (
						supervisorConfigName,
						supervisorConfig);

				}

			}

		}

	}

	void dumpData (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"dumpData");

		) {

			// delete old data

			deleteDirectory (
				"work/console/module");

			forceMkdir (
				"work/console/module");

			// iterate modules

			for (
				Map.Entry<String,ConsoleModule> consoleModuleEntry
					: consoleModules.entrySet ()
			) {

				String consoleModuleBeanName =
					consoleModuleEntry.getKey ();

				ConsoleModule consoleModule =
					consoleModuleEntry.getValue ();

				String outputFileName =
					stringFormat (
						"work/console/module/%s.xml",
						camelToHyphen (
							consoleModuleBeanName));

				try {

					new DataToXml ().writeToFile (
						outputFileName,
						consoleModule);

				} catch (Exception exception) {

					taskLogger.warningFormat (
						"Error writing %s",
						outputFileName);

				}

			}

		}

	}

	void initPaths (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"paths");

		) {

			Map <String, ConsoleContext> contextsByPathPrefix =
				new HashMap<> ();

			for (
				ConsoleContext context
					: consoleContextsByName.values ()
			) {

				if (! context.global ())
					continue;

				if (contextsByPathPrefix.containsKey (
						context.pathPrefix ())) {

					ConsoleContext existingContext =
						contextsByPathPrefix.get (
							context.pathPrefix ());

					taskLogger.errorFormat (
						"Duplicated context path %s for %s and %s",
						context.pathPrefix (),
						context.name (),
						existingContext.name ());

				}

				contextsByPathPrefix.put (
					context.pathPrefix (),
					context);

			}

			paths =
				ImmutableMap.copyOf (
					contextsByPathPrefix.entrySet ().stream ()

				.collect (
					Collectors.toMap (

					entry ->
						entry.getKey (),

					entry ->
						new ContextPathHandler (
							entry.getValue ()))

				)

			);

		}

	}

	private static
	PathSupply pathParts (
			@NonNull String path) {

		List<String> ret =
			new ArrayList<String> ();

		int pos = 0;

		while (path.length () > pos) {

			if (path.length () < pos + 2) {

				throw new RuntimeException (
					stringFormat (
						"Invalid path: '%s'",
						path));

			}

			if (path.charAt (pos) != '/') {

				throw new RuntimeException (
					stringFormat (
						"Invalid path: '%s'",
						path));

			}

			int nextSlash =
				path.indexOf ('/', pos + 1);

			if (nextSlash == -1)
				nextSlash = path.length ();

			if (nextSlash == pos + 1) {

				throw new RuntimeException (
					stringFormat (
						"Invalid path: '%s'",
						path));

			}

			ret.add (
				path.substring (
					pos + 1,
					nextSlash));

			pos = nextSlash;

		}

		return new PathSupply (ret);

	}

	@Override
	public
	void changeContext (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ConsoleContext context,
			@NonNull String contextPartSuffix) {

		try (

			OwnedTransaction transaction =
				database.beginReadOnly (
					logContext,
					parentTaskLogger,
					"changeContext");

		) {

			requestContext.changedContextPath (
				joinWithoutSeparator (
					context.pathPrefix (),
					contextPartSuffix));

			PathSupply pathParts =
				pathParts (
					contextPartSuffix);

			ConsoleContextStuff contextStuff =
				requestContext.consoleContextStuffRequired ();

			contextStuff.reset ();

			context.initContext (
				transaction,
				pathParts,
				contextStuff);

			context.initTabContext (
				contextStuff);

			transaction.close ();

			if (pathParts.size () > 0) {
				throw new RuntimeException ();
			}

			requestContext.consoleContext (
				context);

		}

	}

	@Override
	public
	void runPostProcessors (
			@NonNull Transaction parentTransaction,
			@NonNull String name,
			@NonNull ConsoleContextStuff contextStuff) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"runPostProcessors");

		) {

			ConsoleHelper <?> consoleHelper =
				objectManager.findConsoleHelperRequired (
					name);

			ConsoleHelperProvider <?> consoleHelperProvider =
				consoleHelper.consoleHelperProvider ();

			consoleHelperProvider.postProcess (
				transaction,
				contextStuff);

		}

	}

	/**
	 * Path handler for a context. Calls the context's initContext method then
	 * retrieves the appropriate file and return it.
	 */
	private
	class ContextPathHandler
		implements PathHandler {

		private final
		ConsoleContext consoleContext;

		private
		ContextPathHandler (
				@NonNull ConsoleContext newContext) {

			consoleContext =
				newContext;

		}

		@Override
		public
		WebFile processPath (
				@NonNull TaskLogger parentTaskLogger,
				@NonNull String remainingPath) {

			try (

				OwnedTaskLogger taskLogger =
					logContext.nestTaskLogger (
						parentTaskLogger,
						"ContextPathHandler.processPath");

			) {

				String fullPath =
					joinWithoutSeparator (
						consoleContext.pathPrefix (),
						remainingPath);

				return fileForPath (
					taskLogger,
					fullPath);

			}

		}

	}

	WebFile fileForPath (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String path) {

		try (

			OwnedTransaction transaction =
				database.beginReadOnly (
					logContext,
					parentTaskLogger,
					"fileForPath");

		) {

			transaction.debugFormat (
				"processing path %s",
				path);

			PathSupply pathParts =
				pathParts (path);

			ConsoleContextTab parentContextTab =
				null;

			String firstContextName =
				pathParts.next ();

			ConsoleContext consoleContext =
				context (
					firstContextName,
					true);

			ConsoleContextStuff contextStuff =
				null;

			requestContext.foreignContextPath (
				"");

			while (true) {

				transaction.debugFormat (
					"starting lap with context %s",
					consoleContext.name ());

				try {

					String contextStuffPath =
						stripLastPath (
							pathParts.used ());

					contextStuff =
						contextStuffProvider.provide (
							transaction)

						.foreignPath (
							contextStuffPath)

						.consoleContext (
							consoleContext)

						.parentContextStuff (
							contextStuff)

						.embeddedParentContextTab (
							parentContextTab)

					;

					consoleContext.initContext (
						transaction,
						pathParts,
						contextStuff);

					consoleContext.initTabContext (
						contextStuff);

					transaction.debugFormat (
						"context initialised");

					if (pathParts.size () == 0) {

						transaction.debugFormat (
							"no more path parts, looking up default file");

						String defaultLocalFile =
							consoleContext
								.contextType ()
								.lookupDefaultFileName (requestContext);

						if (
							defaultLocalFile == null
							|| defaultLocalFile.length () == 0
						) {

							transaction.errorFormat (
								"context %s has no default file",
								consoleContext.name ());

							throw new HttpNotFoundException (
								optionalAbsent (),
								emptyList ());

						}

						transaction.debugFormat (
							"got default file %s",
							defaultLocalFile);

						String defaultUrl =
							resolveLocalFile (
								transaction,
								contextStuff,
								contextStuff.consoleContext (),
								defaultLocalFile);

						transaction.debugFormat (
							"redirecting to \"%s\"",
							defaultUrl);

						throw new ExternalRedirectException (
							requestContext.resolveApplicationUrl (
								contextStuff.substitutePlaceholders (
									defaultUrl)));

					}

					if (pathParts.size () == 1) {

						transaction.debugFormat (
							"single path part left, looking up file");

						String file =
							pathParts.next ();

						transaction.debugFormat (
							"file name is %s",
							file);

						requestContext.consoleContext (
							consoleContext);

						requestContext.consoleContextStuff (
							contextStuff);

						WebFile webFile =
							consoleContext
								.files ()
								.get (file);

						if (webFile == null) {

							transaction.errorFormat (
								"context %s has no file %s",
								consoleContext.name (),
								file);

							throw new HttpNotFoundException (
								optionalAbsent (),
								emptyList ());

						}

						transaction.debugFormat (
							"returning successful resolution");

						return webFile;

					}

				} catch (ExternalRedirectException exception) {

					throw exception;

				} catch (Exception exception) {

					throw new RuntimeException (
						stringFormat (
							"error initialising context %s",
							consoleContext.name ()),
						exception);

				}

				// process links

				transaction.debugFormat (
					"multiple remaining path parts, assuming link");

				String link =
					pathParts.next ();

				if (
					stringNotEqualSafe (
						link,
						"-")
				) {
					throw new RuntimeException ();
				}

				requestContext.foreignContextPath (
					pathParts.used ());

				String nextPathPart =
					pathParts.next ();

				transaction.debugFormat (
					"context name from path \"%s\"",
					nextPathPart);

				String contextName =
					"link:" + nextPathPart;

				consoleContext =
					consoleContextsByName.get (
						contextName);

				if (consoleContext == null) {

					transaction.errorFormat (
						"context not found \"%s\"",
						contextName);

					throw new HttpNotFoundException (
						optionalAbsent (),
						emptyList ());

				}

				ConsoleContext searchConsoleContext =
					consoleContext;

				while (
					isNotNull (
						searchConsoleContext.parentContextName ())
				) {

					searchConsoleContext =
						consoleContextsByName.get (
							searchConsoleContext.parentContextName ());

				}

				String parentContextTabName =
					stringFormat (
						"%s",
						searchConsoleContext.name ());

				parentContextTab =
					contextTabs.get (
						parentContextTabName);

				if (parentContextTab == null) {

					throw new RuntimeException (
						stringFormat (
							"no parent tab %s",
							parentContextTabName));

				}

				transaction.debugFormat (
					"link complete");

			}

		}

	}

	private
	String stripLastPath (
			@NonNull String path) {

		int position =
			path.lastIndexOf ('/');

		if (position == -1)
			throw new IllegalArgumentException ();

		return path.substring (0, position);

	}

	@Override
	public
	Optional <ConsoleContext> contextWithParentOfType (
			@NonNull ConsoleContext parentContext,
			@NonNull ConsoleContextType contextType,
			boolean required) {

		List <ConsoleContext> contexts =
			ifNull (
				contextsByParentAndType.get (
					Pair.of (
						parentContext.name (),
						contextType.name ())),
				emptyList ());

		if (contexts.size () > 1) {

			List <String> contextNames =
				new ArrayList<> ();

			for (
				ConsoleContext context
					: contexts
			) {

				contextNames.add (
					context.name ());

			}

			throw new RuntimeException (
				stringFormat (
					"Multiple contexts with parent %s of type %s: %s",
					parentContext.name (),
					contextType.name (),
					joinWithCommaAndSpace (
						contextNames)));

		}

		if (contexts.isEmpty ()) {

			if (required) {

				throw new RuntimeException (
					stringFormat (
						"No context with parent %s of type %s",
						parentContext.name (),
						contextType.name ()));

			} else {

				return optionalAbsent ();

			}

		}

		return Optional.of (
			contexts.get (0));

	}

	@Override
	public
	Optional <ConsoleContext> contextWithParentOfType (
			@NonNull ConsoleContext parentContext,
			@NonNull ConsoleContextType contextType) {

		return contextWithParentOfType (
			parentContext,
			contextType,
			false);

	}

	@Override
	public
	ConsoleContext contextWithParentOfTypeRequired (
			@NonNull ConsoleContext parentContext,
			@NonNull ConsoleContextType contextType) {

		return optionalGetRequired (
			contextWithParentOfType (
				parentContext,
				contextType,
				true));

	}

	@Override
	public
	Optional <ConsoleContext> contextWithoutParentOfType (
			@NonNull ConsoleContextType contextType,
			boolean required) {

		List <ConsoleContext> contexts =
			ifNull (
				contextsWithoutParentByType.get (
					contextType.name ()),
				Collections.<ConsoleContext>emptyList ());

		if (contexts.size () > 1) {

			throw new RuntimeException ();

		}

		if (contexts.size () == 0) {

			if (required) {

				throw new RuntimeException (
					stringFormat (
						"No context of type %s without parent",
						contextType.name ()));

			} else {

				return Optional.<ConsoleContext>absent ();

			}

		}

		return Optional.of (
			contexts.get (0));

	}

	@Override
	public
	Optional<ConsoleContext> contextWithoutParentOfType (
			@NonNull ConsoleContextType contextType) {

		return contextWithoutParentOfType (
			contextType,
			false);

	}

	@Override
	public
	ConsoleContext contextWithoutParentOfTypeRequired (
			@NonNull ConsoleContextType contextType) {

		return optionalGetRequired (
			contextWithoutParentOfType (
				contextType,
				true));

	}

	@Override
	public
	ConsoleContextType contextType (
			@NonNull String contextTypeName,
			boolean required) {

		ConsoleContextType contextType =
			contextTypesByName.get (
				contextTypeName);

		if (contextType == null && required) {

			throw new RuntimeException (
				stringFormat (
					"No such context type: %s",
					contextTypeName));

		}

		return contextType;

	}

	@Override
	public
	String resolveLocalFile (
			@NonNull Transaction parentTransaction,
			@NonNull ConsoleContextStuff contextStuff,
			@NonNull ConsoleContext consoleContext,
			@NonNull String localFile) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"resolveLocalFile");

		) {

			if (localFile.charAt (0) == '/') {

				return joinWithoutSeparator (
					requestContext.applicationPathPrefix (),
					contextStuff.foreignPath (),
					contextStuff.substitutePlaceholders (
						localFile));

			} else if (localFile.startsWith ("type:")) {

				String targetContextTypeName =
					localFile.substring (
						"type:".length ());

				ConsoleContextType targetContextType =
					contextType (
						targetContextTypeName,
						true);

				// try and resolve target context

				Optional<ConsoleContext> targetContext =
					Optional.<ConsoleContext>absent ();

				Optional<ConsoleContext> searchContext =
					Optional.of (
						consoleContext);

				while (
					optionalIsPresent (
						searchContext)
				) {

					targetContext =
						contextWithParentOfType (
							searchContext.get (),
							targetContextType);

					if (
						optionalIsPresent (
							targetContext)
					) {
						break;
					}

					if (
						isNotNull (
							searchContext.get ().parentContextName ())
					) {

						searchContext =
							Optional.of (
								context (
									searchContext.get ().parentContextName (),
									true));

					} else {

						searchContext =
							Optional.<ConsoleContext>absent ();

					}

				}

				if (
					optionalIsNotPresent (
						targetContext)
				) {

					throw new RuntimeException (
						stringFormat (
							"No context of type %s with parent %s",
							targetContextType.name (),
							consoleContext.name ()));

				}

				// create url

				return joinWithoutSeparator (
					requestContext.applicationPathPrefix (),
					contextStuff.foreignPath (),
					targetContext.get ().pathPrefix (),
					consoleContext.localPathForStuff (
						transaction,
						contextStuff));

			} else {

				return joinWithoutSeparator (
					requestContext.applicationPathPrefix (),
					contextStuff.foreignPath (),
					consoleContext.pathPrefix (),
					consoleContext.localPathForStuff (
						transaction,
						contextStuff),
					"/",
					contextStuff.substitutePlaceholders (
						localFile));

			}

		}

	}

	@Override
	public
	Optional <ConsoleContext> relatedContext (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ConsoleContext sourceContext,
			@NonNull ConsoleContextType targetContextType,
			boolean required) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"relatedContext");

		) {

			taskLogger.debugFormat (
				"Choosing related context");

			taskLogger.debugFormat (
				"Target context type: %s",
				targetContextType.name ());

			// get current context

			taskLogger.debugFormat (
				"Source context: %s",
				sourceContext.name ());

			ConsoleContext currentContext =
				sourceContext;

			for (;;) {

				// lookup target context based on parent and type

				Optional <ConsoleContext> targetOptional =
					contextWithParentOfType (
						currentContext,
						targetContextType,
						false);

				if (
					optionalIsPresent (
						targetOptional)
				) {
					return targetOptional;
				}

				// get parent of current context

				if (
					isNull (
						currentContext.parentContextName ())
				) {
					break;
				}

				currentContext =
					context (
						currentContext.parentContextName (),
						true);

			}

			// lookup parent-less target context based on type

			return contextWithoutParentOfType (
				targetContextType,
				required);

		}

	}

	@Override
	public
	SupervisorConfig supervisorConfig (
			String name) {

		return supervisorConfigs.get (
			name);

	}

	@Override
	public
	Map <String, PathHandler> webModulePaths (
			@NonNull TaskLogger parentTaskLogger) {

		return paths;

	}

}
