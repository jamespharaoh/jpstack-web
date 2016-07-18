package wbs.framework.fixtures;

import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;

import wbs.framework.application.scaffold.PluginFixtureSpec;
import wbs.framework.application.scaffold.PluginManager;
import wbs.framework.application.scaffold.PluginSpec;
import wbs.framework.application.tools.BackgroundProcess;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;

@Log4j
public
class FixturesTool {

	// dependencies

	@Inject
	Database database;

	@Inject
	PluginManager pluginManager;

	// collection dependencies

	@Inject
	Map<Class<?>,Provider<FixtureProvider>> fixtureProviderProvidersByClass;

	@Inject
	List<BackgroundProcess> backgroundProcesses;

	// implementation

	public
	void runFixtureProviders (
			List<String> arguments) {

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
