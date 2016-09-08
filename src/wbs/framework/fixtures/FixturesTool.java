package wbs.framework.fixtures;

import static wbs.framework.utils.etc.StringUtils.capitalise;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.scaffold.PluginFixtureSpec;
import wbs.framework.component.scaffold.PluginManager;
import wbs.framework.component.scaffold.PluginSpec;
import wbs.framework.component.tools.BackgroundProcess;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;

@Log4j
public
class FixturesTool {

	// singleton dependencies

	@SingletonDependency
	List <BackgroundProcess> backgroundProcesses;

	@SingletonDependency
	Database database;

	@SingletonDependency
	PluginManager pluginManager;

	// prototype dependencies

	@PrototypeDependency
	Map <Class <?>, Provider <FixtureProvider>> fixtureProviderProvidersByClass;

	// implementation

	public
	void runFixtureProviders (
			@NonNull List <String> arguments) {

		log.info (
			stringFormat (
				"Disabling background processes"));

		backgroundProcesses.forEach (process ->
			process.runAutomatically (
				false));

		log.info (
			stringFormat (
				"About to run fixture providers"));

		for (
			PluginSpec plugin
				: pluginManager.plugins ()
		) {

			for (
				PluginFixtureSpec fixture
					: plugin.fixtures ()
			) {

				log.info (
					stringFormat (
						"About to run fixture provider %s from %s",
						fixture.name (),
						plugin.name ()));

				String fixtureProviderClassName =
					stringFormat (
						"%s.fixture.%sFixtureProvider",
						plugin.packageName (),
						capitalise (
							fixture.name ()));

				Class<?> fixtureProviderClass;

				try {

					fixtureProviderClass =
						Class.forName (
							fixtureProviderClassName);

				} catch (ClassNotFoundException exception) {

					throw new RuntimeException (
						stringFormat (
							"Can't find fixture provider of type %s for ",
							fixtureProviderClassName,
							"fixture %s ",
							fixture.name (),
							"from %s",
							plugin.name ()));

				}

				Provider<FixtureProvider> fixtureProviderProvider =
					fixtureProviderProvidersByClass.get (
						fixtureProviderClass);

				FixtureProvider fixtureProvider =
					fixtureProviderProvider.get ();

				try {

					@Cleanup
					Transaction transaction =
						database.beginReadWrite (
							"FixturesTool.runFixtureProviders (arguments)",
							this);

					fixtureProvider.createFixtures ();

					transaction.commit ();

				} catch (Exception exception) {

					throw new RuntimeException (
						stringFormat (
							"Error creating fixture %s from %s",
							fixture.name (),
							plugin.name ()),
						exception);

				}

			}

		}

		log.info (
			stringFormat (
				"All fixtures providers run successfully"));

	}

}
