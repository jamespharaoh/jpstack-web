package wbs.framework.fixtures;

import static wbs.utils.etc.TypeUtils.classNameFull;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import lombok.NonNull;

import wbs.framework.component.registry.ComponentDefinition;
import wbs.framework.component.registry.ComponentRegistryBuilder;
import wbs.framework.component.scaffold.PluginFixtureSpec;
import wbs.framework.component.scaffold.PluginSpec;
import wbs.framework.component.tools.ComponentPlugin;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.LoggingLogic;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

public
class FixtureComponentPlugin
	implements ComponentPlugin {

	// state

	LogContext logContext;

	// constructors

	public
	FixtureComponentPlugin (
			@NonNull LoggingLogic loggingLogic) {

		logContext =
			loggingLogic.findOrCreateLogContext (
				classNameFull (
					getClass ()));

	}

	// public implementation

	@Override
	public
	void registerComponents (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ComponentRegistryBuilder componentRegistry,
			@NonNull PluginSpec plugin) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"registerComponents");

		) {

			for (
				PluginFixtureSpec fixture
					: plugin.fixtures ()
			) {

				String fixtureProviderComponentName =
					stringFormat (
						"%sFixtureProvider",
						fixture.name ());

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
						"Can't find fixture provider of type %s ",
						fixtureProviderClassName,
						"for fixture %s ",
						fixture.name (),
						"from %s",
						plugin.name ());

					continue;

				}

				componentRegistry.registerDefinition (
					taskLogger,
					new ComponentDefinition ()

					.name (
						fixtureProviderComponentName)

					.componentClass (
						fixtureProviderClass)

					.scope (
						"prototype"));

			}

		}

	}

}
