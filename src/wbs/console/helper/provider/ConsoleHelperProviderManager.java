package wbs.console.helper.provider;

import static wbs.utils.etc.TypeUtils.classNameFull;

import java.util.HashMap;
import java.util.Map;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

@SingletonComponent ("consoleHelperProviderManager")
public
class ConsoleHelperProviderManager {

	// singleton dependencies

	@SingletonDependency
	Map <String, ConsoleHelperProvider <?>> consoleHelperProvidersByBeanName;

	@ClassSingletonDependency
	LogContext logContext;

	// state

	Map <Class <?>, ConsoleHelperProvider <?>> consoleHelperProvidersByClass =
		new HashMap <Class <?>, ConsoleHelperProvider <?>> ();

	// life cycle

	@NormalLifecycleSetup
	public
	void init (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"init");

		for (
			Map.Entry <String, ConsoleHelperProvider <?>> entry
				: consoleHelperProvidersByBeanName.entrySet ()
		) {

			ConsoleHelperProvider <?> consoleHelperProvider =
				entry.getValue ();

			// check for dupes

			if (
				consoleHelperProvidersByClass.containsKey (
					consoleHelperProvider.objectClass ())
			) {

				taskLogger.errorFormat (
					"Ignoring duplicate helper provider for class %s",
					classNameFull (
						consoleHelperProvider.objectClass ()));

				continue;

			}

			// store in indexes

			consoleHelperProvidersByClass.put (
				consoleHelperProvider.objectClass (),
				consoleHelperProvider);

		}

	}

}
