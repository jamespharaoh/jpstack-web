package wbs.framework.component.scaffold;

import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;

@Log4j
@Accessors (fluent = true)
public
class PluginManager {

	// state

	@Getter @Setter
	List <PluginSpec> plugins;

	@Getter @Setter
	Map <String, PluginModelSpec> pluginModelsByName;

	@Getter @Setter
	Map <String, PluginEnumTypeSpec> pluginEnumTypesByName;

	@Getter @Setter
	Map <String, PluginCustomTypeSpec> pluginCustomTypesByName;

	// implementation

	@Accessors (fluent = true)
	public static
	class Builder {

		// properties

		@Setter
		List <PluginSpec> plugins;

		// state

		Set <String> donePluginNames;

		// implementation

		public
		PluginManager build () {

			ImmutableList.Builder <PluginSpec> pluginsBuilder =
				ImmutableList.builder ();

			ImmutableMap.Builder <String, PluginModelSpec>
			pluginModelsByNameBuilder =
				ImmutableMap.builder ();

			ImmutableMap.Builder <String, PluginEnumTypeSpec>
			pluginEnumTypesByNameBuilder =
				ImmutableMap.builder ();

			ImmutableMap.Builder <String, PluginCustomTypeSpec>
			pluginCustomTypesByNameBuilder =
				ImmutableMap.builder ();

			donePluginNames =
				new HashSet<> ();

			List <PluginSpec> remainingPlugins =
				new LinkedList<> (
					plugins);

			Collections.sort (
				remainingPlugins);

			OUTER:
			for (;;) {

				ListIterator <PluginSpec> iterator =
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


					for (
						PluginModelSpec pluginModel
							: plugin.models ().models ()
					) {

						pluginModelsByNameBuilder.put (
							pluginModel.name (),
							pluginModel);

					}

					for (
						PluginEnumTypeSpec pluginEnumType
							: plugin.models ().enumTypes ()
					) {

						pluginEnumTypesByNameBuilder.put (
							pluginEnumType.name (),
							pluginEnumType);

					}

					for (
						PluginCustomTypeSpec pluginCustomType
							: plugin.models ().customTypes ()
					) {

						pluginCustomTypesByNameBuilder.put (
							pluginCustomType.name (),
							pluginCustomType);

					}

					donePluginNames.add (
						plugin.name ());

					iterator.remove ();

					log.debug (
						stringFormat (
							"Resolved dependencies for plugin %s",
							plugin.name ()));

					continue OUTER;

				}

				break;

			}

			if (! remainingPlugins.isEmpty ()) {

				for (
					PluginSpec plugin
						: remainingPlugins
				) {

					log.error (
						stringFormat (
							"Unable to resolve dependencies for plugin %s",
							plugin.name ()));

				}

				throw new RuntimeException (
					stringFormat (
						"Aborting due to %s errors",
						remainingPlugins.size ()));

			}

			return new PluginManager ()

				.plugins (
					pluginsBuilder.build ())

				.pluginModelsByName (
					pluginModelsByNameBuilder.build ())

				.pluginEnumTypesByName (
					pluginEnumTypesByNameBuilder.build ())

				.pluginCustomTypesByName (
					pluginCustomTypesByNameBuilder.build ());

		}

		boolean pluginDependenciesSatisfied (
				PluginSpec plugin) {

			for (
				PluginDependencySpec pluginDependency
					: plugin.pluginDependencies ()
			) {

				if (! donePluginNames.contains (
						pluginDependency.name ()))
					return false;

			}

			return true;

		}

	}

}
