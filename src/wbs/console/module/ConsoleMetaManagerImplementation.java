package wbs.console.module;

import static wbs.framework.utils.etc.Misc.contains;
import static wbs.framework.utils.etc.NullUtils.ifNull;
import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.StringUtils.camelToHyphen;
import static wbs.framework.utils.etc.StringUtils.naivePluralise;
import static wbs.framework.utils.etc.StringUtils.stringEqual;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.commons.io.FileUtils;

import com.google.common.collect.ImmutableList;

import lombok.NonNull;
import lombok.extern.log4j.Log4j;
import wbs.console.context.ConsoleContextExtensionPoint;
import wbs.console.context.ConsoleContextHint;
import wbs.console.context.ConsoleContextLink;
import wbs.console.context.ResolvedConsoleContextExtensionPoint;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.data.tools.DataToXml;

@Log4j
@SingletonComponent ("consoleMetaManagerImpl")
public
class ConsoleMetaManagerImplementation
	implements ConsoleMetaManager {

	// prototype dependencies

	@Inject
	Provider<ResolvedConsoleContextLink> resolvedContextLinkProvider;

	// collection dependencies

	@Inject
	Map<String,ConsoleMetaModule> consoleMetaModules;

	// state

	Map<String,List<ConsoleContextLink>> contextLinks =
		new HashMap<> ();

	Map<String,List<ConsoleContextExtensionPoint>> extensionPoints =
		new HashMap<> ();

	Map<String,ConsoleContextHint> contextHints =
		new HashMap<> ();

	// init

	@PostConstruct
	public
	void init () {

		// reset output dir

		try {

			FileUtils.deleteDirectory (
				new File (
					"work/console/meta-module"));

			FileUtils.forceMkdir (
				new File (
					"work/console/meta-module"));

		} catch (IOException exception) {

			log.error (
				"Error deleting contents of work/console/meta-module",
				exception);

		}

		// collect stuff

		for (
			Map.Entry<String,ConsoleMetaModule> consoleMetaModuleEntry
				: consoleMetaModules.entrySet ()
		) {

			String consoleMetaModuleName =
				consoleMetaModuleEntry.getKey ();

			ConsoleMetaModule consoleMetaModule =
				consoleMetaModuleEntry.getValue ();

			// collect context links

			for (
				ConsoleContextLink contextLink
					: consoleMetaModule.contextLinks ()
			) {

				List<ConsoleContextLink> contextLinksForName =
					contextLinks.get (
						contextLink.linkName ());

				if (contextLinksForName == null) {

					contextLinksForName =
						new ArrayList<ConsoleContextLink> ();

					contextLinks.put (
						contextLink.linkName (),
						contextLinksForName);

				}

				contextLinksForName.add (
					contextLink);

			}

			// collect extension points

			for (
				ConsoleContextExtensionPoint extensionPoint
					: consoleMetaModule.extensionPoints ()
			) {

				List<ConsoleContextExtensionPoint> extensionPointsForName =
					extensionPoints.get (
						extensionPoint.name ());

				if (extensionPointsForName == null) {

					extensionPointsForName =
						new ArrayList<ConsoleContextExtensionPoint> ();

					extensionPoints.put (
						extensionPoint.name (),
						extensionPointsForName);

				}

				extensionPointsForName.add (
					extensionPoint);

			}

			// collect context hints

			for (
				ConsoleContextHint contextHint
					: consoleMetaModule.contextHints ()
			) {

				if (
					contains (
						contextHints,
						contextHint.linkName ())
				) {
					throw new RuntimeException ();
				}

				contextHints.put (
					contextHint.linkName (),
					contextHint);

			}

			// dump out data

			String outputFileName =
				stringFormat (
					"work/console/meta-module/%s.xml",
					camelToHyphen (
						consoleMetaModuleName));

			try {

				new DataToXml ().writeToFile (
					outputFileName,
					consoleMetaModule);

			} catch (IOException exception) {

				log.warn (
					stringFormat (
						"Error writing %s",
						outputFileName));

			}

		}

	}

	// implementation

	@Override
	public
	List<ResolvedConsoleContextExtensionPoint> resolveExtensionPoint (
			@NonNull String name) {

		List<ResolvedConsoleContextExtensionPoint> resolvedExtensionPoints =
			new ArrayList<ResolvedConsoleContextExtensionPoint> ();

		List<ConsoleContextExtensionPoint> extensionPointsForName =
			ifNull (
				extensionPoints.get (name),
				Collections.<ConsoleContextExtensionPoint>emptyList ());

		for (
			ConsoleContextExtensionPoint extensionPoint
				: extensionPointsForName
		) {

			if (extensionPoint.root ()) {

				resolvedExtensionPoints.add (
					new ResolvedConsoleContextExtensionPoint ()

					.name (
						extensionPoint.name ())

					.parentContextNames (
						ImmutableList.<String>builder ()

						.addAll (
							extensionPoint.parentContextNames ())

						.addAll (
							parentContextNames (
								extensionPoint.contextLinkNames ()))

						.build ())

					.contextTypeNames (
						extensionPoint.contextTypeNames ())

					.contextLinkNames (
						extensionPoint.contextLinkNames ()));

			} else if (extensionPoint.nested ()) {

				if (
					stringEqual (
						extensionPoint.name (),
						extensionPoint.parentExtensionPointName ())
				) {

					throw new RuntimeException (
						stringFormat (
							"Extension point %s is its own parent",
							extensionPoint.name ()));

				}

				resolvedExtensionPoints.addAll (
					resolveExtensionPoint (
						extensionPoint.parentExtensionPointName ()));

			} else {

				throw new RuntimeException ();

			}

		}

		return resolvedExtensionPoints;

	}

	List<String> parentContextNames (
			@NonNull List<String> contextLinkNames) {

		ImmutableList.Builder<String> parentContextNamesBuilder =
			ImmutableList.<String>builder ();

		for (
			String contextLinkName
				: contextLinkNames
		) {

			for (
				ResolvedConsoleContextLink resolvedContextLink
					: resolveContextLink (contextLinkName)
			) {

				for (
					String parentContextName
						: resolvedContextLink.parentContextNames ()
				) {

					ConsoleContextHint contextHint =
						contextHints.get (
							contextLinkName);

					if (
						isNull (
							contextHint)
					) {

						throw new RuntimeException (
							stringFormat (
								"No context hint for context link name: %s",
								contextLinkName));

					}

					if (contextHint.singular ()) {

						parentContextNamesBuilder.add (
							stringFormat (
								"%s.%s",
								parentContextName,
								resolvedContextLink.localName ()));

					}

					if (contextHint.plural ()) {

						parentContextNamesBuilder.add (
							stringFormat (
								"%s.%s",
								parentContextName,
								naivePluralise (
									resolvedContextLink.localName ())));

					}

				}

			}

		}

		return parentContextNamesBuilder.build ();

	}

	@Override
	public
	List<ResolvedConsoleContextLink> resolveContextLink (
			@NonNull String contextLinkName) {

		List<ResolvedConsoleContextLink> resolvedContextLinks =
			new ArrayList<ResolvedConsoleContextLink> ();

		List<ConsoleContextLink> contextLinksForName =
			ifNull (
				contextLinks.get (contextLinkName),
				Collections.<ConsoleContextLink>emptyList ());

		for (
			ConsoleContextLink contextLink
				: contextLinksForName
		) {

			for (
				ResolvedConsoleContextExtensionPoint resolvedExtensionPoint
					: resolveExtensionPoint (
						contextLink.extensionPointName ())
			) {

				// generate

				resolvedContextLinks.add (
					resolvedContextLinkProvider.get ()

					.name (
						stringFormat (
							"%s.%s",
							resolvedExtensionPoint.name (),
							contextLink.localName ()))

					.localName (
						contextLink.localName ())

					.tabName (
						stringFormat (
							"%s.%s",
							resolvedExtensionPoint.name (),
							contextLink.localName ()))

					.tabLocation (
						contextLink.tabLocation ())

					.tabLabel (
						contextLink.label ())

					.tabPrivKey (
						contextLink.privKey ())

					.tabContextTypeNames (
						resolvedExtensionPoint.contextTypeNames ())

					.parentContextNames (
						resolvedExtensionPoint.parentContextNames ())

				);

			}

		}

		return resolvedContextLinks;

	}

}
