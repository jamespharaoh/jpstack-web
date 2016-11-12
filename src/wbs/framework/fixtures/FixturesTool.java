package wbs.framework.fixtures;

import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.scaffold.PluginFixtureSpec;
import wbs.framework.component.scaffold.PluginManager;
import wbs.framework.component.scaffold.PluginSpec;
import wbs.framework.component.tools.BackgroundProcess;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

public
class FixturesTool {

	// singleton dependencies

	@SingletonDependency
	List <BackgroundProcess> backgroundProcesses;

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	PluginManager pluginManager;

	// prototype dependencies

	@PrototypeDependency
	Map <Class <?>, Provider <FixtureProvider>> fixtureProviderProvidersByClass;

	// implementation

	public
	void runFixtureProviders (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull List <String> arguments) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"runFixtureProviders");

		taskLogger.noticeFormat (
			"Disabling background processes");

		backgroundProcesses.forEach (process ->
			process.runAutomatically (
				false));

		taskLogger.noticeFormat (
			"About to run fixture providers");

		for (
			PluginSpec plugin
				: pluginManager.plugins ()
		) {

			for (
				PluginFixtureSpec fixture
					: plugin.fixtures ()
			) {

				taskLogger.noticeFormat (
					"About to run fixture provider %s from %s",
					fixture.name (),
					plugin.name ());

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

					taskLogger.errorFormat (
						"Can't find fixture provider of type %s for ",
						fixtureProviderClassName,
						"fixture %s ",
						fixture.name (),
						"from %s",
						plugin.name ());

					continue;

				}

				Provider<FixtureProvider> fixtureProviderProvider =
					fixtureProviderProvidersByClass.get (
						fixtureProviderClass);

				FixtureProvider fixtureProvider =
					fixtureProviderProvider.get ();

				try (

					Transaction transaction =
						database.beginReadWrite (
							"FixturesTool.runFixtureProviders (arguments)",
							this);

				) {

					fixtureProvider.createFixtures ();

					transaction.commit ();

				} catch (Exception exception) {

					taskLogger.errorFormatException (
						exception,
						"Error creating fixture %s from %s",
						fixture.name (),
						plugin.name ());

				}

			}

		}

		taskLogger.noticeFormat (
			"All fixtures providers run successfully");

	}

}
