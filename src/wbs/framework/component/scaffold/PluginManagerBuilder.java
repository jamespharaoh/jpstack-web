package wbs.framework.component.scaffold;

import static wbs.utils.collection.CollectionUtils.collectionIsNotEmpty;
import static wbs.utils.collection.IterableUtils.iterableFilterToList;
import static wbs.utils.collection.IterableUtils.iterableMap;
import static wbs.utils.etc.Misc.doesNotContain;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.joinWithCommaAndSpace;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.inject.Provider;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("pluginManagerBuilder")
@Accessors (fluent = true)
public
class PluginManagerBuilder {

	// singleton depndencies

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <PluginManager> pluginManagerProvider;

	// properties

	@Setter
	List <PluginSpec> plugins;

	// state

	Set <String> donePluginNames;

	// implementation

	public
	PluginManager build (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"build");

		) {

			verifyPluginDependencies (
				taskLogger);

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

					taskLogger.debugFormat (
						"Resolved dependencies for plugin %s",
						plugin.name ());

					continue OUTER;

				}

				break;

			}

			if (! remainingPlugins.isEmpty ()) {

				for (
					PluginSpec plugin
						: remainingPlugins
				) {

					taskLogger.errorFormat (
						"Unable to resolve dependencies for plugin %s",
						plugin.name ());

				}

				throw new RuntimeException (
					stringFormat (
						"Aborting due to %s errors",
						integerToDecimalString (
							remainingPlugins.size ())));

			}

			return pluginManagerProvider.get ()

				.plugins (
					pluginsBuilder.build ())

				.pluginModelsByName (
					pluginModelsByNameBuilder.build ())

				.pluginEnumTypesByName (
					pluginEnumTypesByNameBuilder.build ())

				.pluginCustomTypesByName (
					pluginCustomTypesByNameBuilder.build ())

			;

		}

	}

	private
	void verifyPluginDependencies (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"verifyPluginDependencies");

		) {

			Set <String> allPluginNames =
				ImmutableSet.copyOf (
					iterableMap (
						PluginSpec::name,
						plugins));

			for (
				PluginSpec plugin
					: plugins
			) {

				List <String> missingDependencies =
					iterableFilterToList (
						dependencyName ->
							doesNotContain (
								allPluginNames,
								dependencyName),
						iterableMap (
							PluginDependencySpec::name,
							plugin.pluginDependencies ()));

				if (
					collectionIsNotEmpty (
						missingDependencies)
				) {

					taskLogger.errorFormat (
						"Plugin %s missing dependencies: %s",
						plugin.name (),
						joinWithCommaAndSpace (
							missingDependencies));

				}

			}

			taskLogger.makeException ();

		}

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
