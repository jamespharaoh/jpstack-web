package wbs.framework.application.scaffold;

import static wbs.framework.utils.etc.Misc.joinWithSeparator;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;

import com.google.common.collect.ImmutableList;

@Log4j
@Accessors (fluent = true)
public
class PluginManager {

	// state

	@Getter @Setter
	List<PluginSpec> plugins;

	// implementation

	@Accessors (fluent = true)
	public static
	class Builder {

		// properties

		@Setter
		List<PluginSpec> plugins;

		// state

		Set<String> donePluginNames;

		// implementation

		public
		PluginManager build () {

			ImmutableList.Builder<PluginSpec> pluginsBuilder =
				ImmutableList.<PluginSpec>builder ();

			donePluginNames =
				new HashSet<String> ();

			List<PluginSpec> remainingPlugins =
				new LinkedList<PluginSpec> (
					plugins);

			Collections.sort (
				remainingPlugins);

			OUTER:
			for (;;) {

				ListIterator<PluginSpec> iterator =
					remainingPlugins.listIterator ();

				while (iterator.hasNext ()) {

					PluginSpec plugin =
						iterator.next ();

					if (
						! pluginDependenciesSatisfied (
							plugin)
					) {
						continue;
					}

					pluginsBuilder.add (
						plugin);

					donePluginNames.add (
						joinWithSeparator (
							".",
							plugin.project ().name (),
							plugin.name ()));

					iterator.remove ();

					log.info (
						stringFormat (
							"Resolved dependencies for plugin %s/%s",
							plugin.project ().name (),
							plugin.name ()));

					continue OUTER;

				}

				break;

			}

			if (! remainingPlugins.isEmpty ()) {

				for (PluginSpec plugin
						: remainingPlugins) {

					log.error (
						stringFormat (
							"Unable to resolve dependencies for plugin %s/%s",
							plugin.project ().name (),
							plugin.name ()));

				}

				throw new RuntimeException (
					stringFormat (
						"Aborting due to %s errors",
						remainingPlugins.size ()));

			}

			return new PluginManager ()

				.plugins (
					pluginsBuilder.build ());

		}

		boolean pluginDependenciesSatisfied (
				PluginSpec plugin) {

			for (PluginProjectDependencySpec projectDependency
					: plugin.dependencies ().projects ()) {

				for (PluginPluginDependencySpec pluginDependency
						: projectDependency.plugins ()) {

					String dependencyName =
						joinWithSeparator (
							".",
							projectDependency.name (),
							pluginDependency.name ());

					if (! donePluginNames.contains (
							dependencyName))
						return false;

				}

			}

			return true;

		}

	}

}
