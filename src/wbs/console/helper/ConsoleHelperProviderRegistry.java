package wbs.console.helper;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.HashMap;
import java.util.Map;

import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.entity.record.Record;

@SingletonComponent ("consoleHelperProviderRegistry")
@Log4j
public
class ConsoleHelperProviderRegistry {

	Map <String, ConsoleHelperProvider <?>> byObjectName =
		new HashMap<> ();

	Map <Class <?>, ConsoleHelperProvider <?>> byObjectClass =
		new HashMap<> ();

	public
	void register (
			@NonNull ConsoleHelperProvider <?> consoleHelperProvider) {

		// detect dupes

		if (
			byObjectName.containsKey (
				consoleHelperProvider.objectName ())
		) {

			throw new RuntimeException (
				stringFormat (
					"Got multiple console helper providers for %s",
					consoleHelperProvider.objectName ()));

		}

		if (
			byObjectClass.containsKey (
				consoleHelperProvider.objectClass ())
		) {

			throw new RuntimeException (
				stringFormat (
					"Got multiple console helper providers for %s",
					consoleHelperProvider.objectClass ()));

		}

		// store

		log.debug (
			stringFormat (
				"Registering console helper provider for %s",
				consoleHelperProvider.objectName ()));

		byObjectName.put (
			consoleHelperProvider.objectName (),
			consoleHelperProvider);

		byObjectClass.put (
			consoleHelperProvider.objectClass (),
			consoleHelperProvider);

	}

	public
	ConsoleHelperProvider<?> findByObjectClass (
			Class<?> objectClass) {

		return byObjectClass.get (
			objectClass);

	}

	public
	ConsoleHelperProvider<?> findByObjectName (
			String objectName) {

		return byObjectName.get (
			objectName);

	}

	public
	ConsoleHelperProvider<?> findByObject (
			@NonNull Record<?> object) {

		Class<?> objectClass =
			object.getClass ();

		while (Record.class.isAssignableFrom (objectClass)) {

			ConsoleHelperProvider<?> consoleHelperProvider =
				findByObjectClass (objectClass);

			if (consoleHelperProvider != null)
				return consoleHelperProvider;

			objectClass =
				objectClass.getSuperclass ();

		}

		return null;

	}

}
