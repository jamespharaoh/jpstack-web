package wbs.console.helper;

import javax.inject.Provider;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;

import wbs.console.module.ConsoleMetaManager;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectHelper;

@Accessors (fluent = true)
@Log4j
public
class ConsoleHelperFactory
	implements ComponentFactory {

	// dependencies

	@SingletonDependency
	ConsoleHelperProviderRegistry consoleHelperProviderRegistry;

	@SingletonDependency
	ConsoleMetaManager consoleMetaManager;

	@SingletonDependency
	ConsoleHelperRegistry consoleHelperRegistry;

	// prototype dependencies

	@PrototypeDependency
	Provider <ConsoleHelperBuilder> consoleHelperBuilder;

	@PrototypeDependency
	Provider <GenericConsoleHelperProvider>
	genericConsoleHelperProviderProvider;

	// required properties

	@Getter @Setter
	ObjectHelper <?> objectHelper;

	@Getter @Setter
	Class <? extends ConsoleHelper <?>> consoleHelperClass;

	// implementation

	@Override
	public
	Object makeComponent (
			@NonNull TaskLogger taskLogger) {

		taskLogger =
			taskLogger.nest (
				this,
				"makeComponent",
				log);

		if (consoleHelperClass == null)
			throw new NullPointerException ("consoleHelperClass");

		ConsoleHelperProvider <?> consoleHelperProvider =
			consoleHelperProviderRegistry.findByObjectName (
				objectHelper.objectName ());

		if (consoleHelperProvider == null) {

			taskLogger.warningFormat (
				"No console helper provider for %s",
				objectHelper.objectName ());

			consoleHelperProvider =
				genericConsoleHelperProviderProvider.get ()

				.consoleHelperProviderSpec (
					new ConsoleHelperProviderSpec ())

				.objectHelper (
					objectHelper)

				.consoleHelperClass (
					consoleHelperClass)

				.init ();

		}

		taskLogger.debugFormat (
			"Getting console helper %s for %s",
			consoleHelperProvider.objectName (),
			consoleHelperProvider.objectClass ().getSimpleName ());

		ConsoleHelper <?> consoleHelper =
			consoleHelperBuilder.get ()

			.objectHelper (
				objectHelper)

			.consoleHelperClass (
				consoleHelperClass)

			.consoleHelperProvider (
				consoleHelperProvider)

			.build ();

		consoleHelperRegistry.register (
			consoleHelper);

		return consoleHelper;

	}

}
