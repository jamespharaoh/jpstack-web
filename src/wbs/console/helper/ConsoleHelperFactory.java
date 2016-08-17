package wbs.console.helper;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;
import wbs.console.module.ConsoleMetaManager;
import wbs.framework.application.context.UninitializedComponentFactory;
import wbs.framework.object.ObjectHelper;

@Accessors (fluent = true)
@Log4j
public
class ConsoleHelperFactory
	implements UninitializedComponentFactory {

	// dependencies

	@Inject
	ConsoleHelperProviderRegistry consoleHelperProviderRegistry;

	@Inject
	ConsoleMetaManager consoleMetaManager;

	// prototype dependencies

	@Inject
	Provider<ConsoleHelperBuilder> consoleHelperBuilder;

	@Inject
	Provider<ConsoleHelperRegistry> consoleHelperRegistry;

	@Inject
	Provider<GenericConsoleHelperProvider> genericConsoleHelperProviderProvider;

	// required properties

	@Getter @Setter
	ObjectHelper<?> objectHelper;

	@Getter @Setter
	Class<? extends ConsoleHelper<?>> consoleHelperClass;

	// implementation

	@Override
	public
	Object makeComponent () {

		if (consoleHelperClass == null)
			throw new NullPointerException ("consoleHelperClass");

		ConsoleHelperProvider<?> consoleHelperProvider =
			consoleHelperProviderRegistry.findByObjectName (
				objectHelper.objectName ());

		if (consoleHelperProvider == null) {

			log.warn (
				stringFormat (
					"No console helper provider for %s",
					objectHelper.objectName ()));

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

		log.debug (
			stringFormat (
				"Getting console helper %s for %s",
				consoleHelperProvider.objectName (),
				consoleHelperProvider.objectClass ().getSimpleName ()));

		ConsoleHelper<?> consoleHelper =
			consoleHelperBuilder.get ()

			.objectHelper (
				objectHelper)

			.consoleHelperClass (
				consoleHelperClass)

			.consoleHelperProvider (
				consoleHelperProvider)

			.build ();

		consoleHelperRegistry.get ().register (
			consoleHelper);

		return consoleHelper;

	}

}
