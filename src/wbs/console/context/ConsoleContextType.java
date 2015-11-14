package wbs.console.context;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;

import wbs.console.request.ConsoleRequestContext;
import wbs.console.tab.ConsoleContextTab;
import wbs.console.tab.ContextTabPlacement;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.web.WebFile;

@Accessors (fluent = true)
@Log4j
@DataClass ("context-type")
@PrototypeComponent ("concoleContextType")
public
class ConsoleContextType {

	@DataAttribute
	@Getter @Setter
	String name;

	@Getter @Setter
	List<ContextTabPlacement> tabPlacements =
		new ArrayList<ContextTabPlacement> ();

	@Getter @Setter
	Map<String,ConsoleContextTab> tabs =
		new LinkedHashMap<String,ConsoleContextTab> ();

	@Getter @Setter
	Map<String,WebFile> files =
		new LinkedHashMap<String,WebFile> ();

	@Getter @Setter
	String defaultFileName;

	public
	String lookupDefaultFileName (
			ConsoleRequestContext requestContext) {

		if (tabs.isEmpty ()) {

			throw new RuntimeException (
				stringFormat (
					"No tabs from which to select default for \"%s\"",
					name ()));

		}

		// look for specified default page

		if (defaultFileName != null) {

			for (
				ConsoleContextTab contextTab
					: tabs.values ()
			) {

				if (
					contextTab.name ().endsWith (
						"." + defaultFileName)
				) {

					return contextTab.localFile ();

				}

			}

		}

		// look for settings or summary page

		for (
			ConsoleContextTab contextTab
				: tabs.values ()
		) {

			if (contextTab.name ().endsWith (".summary"))
				return contextTab.localFile ();

			if (contextTab.name ().endsWith (".settings"))
				return contextTab.localFile ();

		}

		// return first tab

		return tabs
			.values ()
			.iterator ()
			.next ()
			.localFile ();

	}

	public
	void resolveTabSpecs (
			Map<String,ConsoleContextTab> allContextTabs) {

		List<String> resolvedNames =
			new LinkedList<String> ();

		resolvedNames.add ("+end");

		List<ContextTabPlacement> unresolvedPlacements =
			new ArrayList<ContextTabPlacement> (
				tabPlacements ());

		while (! unresolvedPlacements.isEmpty ()) {

			boolean madeProgress = false;

			Iterator<ContextTabPlacement> tabPlacementIterator =
				unresolvedPlacements.listIterator ();

			while (tabPlacementIterator.hasNext ()) {

				ContextTabPlacement tabPlacement =
					tabPlacementIterator.next ();

				String tabLocation =
					tabPlacement.tabLocation ();

				String tabName =
					tabPlacement.tabName ();

				int position =
					resolvedNames.indexOf (
						"+" + tabLocation);

				if (position < 0)
					continue;

				resolvedNames.add (
					position,
					tabName);

				tabPlacementIterator.remove ();

				madeProgress = true;

			}

			if (! madeProgress) {

				for (ContextTabPlacement tabPlacement
						: unresolvedPlacements) {

					String tabLocation =
						tabPlacement.tabLocation ();

					String tabName =
						tabPlacement.tabName ();

					log.error (
						stringFormat (
							"Location %s not found for tab %s in %s",
							tabLocation,
							tabName,
							name));

				}

				throw new RuntimeException (
					stringFormat (
						"Unable to resolve %s tab locations",
						unresolvedPlacements.size ()));

			}

		}

		Map<String,ConsoleContextTab> myNewContextTabs =
			new LinkedHashMap<String,ConsoleContextTab> ();

		for (String tabSpec
				: resolvedNames) {

			if (tabSpec.charAt (0) == '+')
				continue;

			myNewContextTabs.put (
				tabSpec,
				allContextTabs.get (tabSpec));

		}

		tabs =
			myNewContextTabs;

	}

}
