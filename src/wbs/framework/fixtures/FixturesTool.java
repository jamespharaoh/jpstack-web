package wbs.framework.fixtures;

import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.TypeUtils.classForName;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.activitymanager.ActivityManager;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.scaffold.PluginFixtureSpec;
import wbs.framework.component.scaffold.PluginManager;
import wbs.framework.component.scaffold.PluginSpec;
import wbs.framework.component.tools.BackgroundProcess;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

public
class FixturesTool {

	// singleton dependencies

	@SingletonDependency
	ActivityManager activityManager;

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

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"runFixtureProviders");

		) {

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

					runFixtureProvider (
						taskLogger,
						plugin,
						fixture);

				}

			}

			taskLogger.noticeFormat (
				"All fixtures providers run successfully");

		}

	}

	private
	void runFixtureProvider (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull PluginSpec plugin,
			@NonNull PluginFixtureSpec fixture) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"runFixtureProvider");

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

			Optional <Class <?>> fixtureProviderClassOptional =
				classForName (
					fixtureProviderClassName);

			if (
				optionalIsNotPresent (
					fixtureProviderClassOptional)
			) {

				taskLogger.errorFormat (
					"Can't find fixture provider of type %s for ",
					fixtureProviderClassName,
					"fixture %s ",
					fixture.name (),
					"from %s",
					plugin.name ());

				return;

			}

			Class <?> fixtureProviderClass =
				optionalGetRequired (
					fixtureProviderClassOptional);

			Provider <FixtureProvider> fixtureProviderProvider =
				fixtureProviderProvidersByClass.get (
					fixtureProviderClass);

			FixtureProvider fixtureProvider =
				fixtureProviderProvider.get ();

			try (

				OwnedTransaction transaction =
					database.beginReadWrite (
						taskLogger,
						"FixturesTool.runFixtureProviders (arguments)",
						this);

			) {

				fixtureProvider.createFixtures (
					taskLogger,
					transaction);

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

}
