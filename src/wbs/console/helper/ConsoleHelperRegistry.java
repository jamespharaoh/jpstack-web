package wbs.console.helper;

import static wbs.utils.string.StringUtils.stringFormat;

import java.util.HashMap;
import java.util.Map;

import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import wbs.framework.component.annotations.SingletonComponent;

@SingletonComponent ("consoleHelperRegistry")
@Log4j
public
class ConsoleHelperRegistry {

	Map <String, ConsoleHelper <?>> byObjectName =
		new HashMap<> ();

	Map <Class <?>, ConsoleHelper <?>> byObjectClass =
		new HashMap<> ();

	public synchronized
	void register (
			@NonNull ConsoleHelper <?> consoleHelper) {

		// detect dupes

		if (
			byObjectName.containsKey (
				consoleHelper.objectName ())
		) {

			log.warn (
				stringFormat (
					"Not registering duplicate console helper object name %s",
					consoleHelper.objectName ()));

			return;

		}

		if (
			byObjectClass.containsKey (
				consoleHelper.objectClass ())
		) {

			log.warn (
				stringFormat (
					"Not registering duplicate console helper object class %s",
					consoleHelper.objectClass ().getName ()));

			return;

		}

		// store

		log.debug (
			stringFormat (
				"Registering console helper %s",
				consoleHelper.objectName ()));

		byObjectName.put (
			consoleHelper.objectName (),
			consoleHelper);

		byObjectClass.put (
			consoleHelper.objectClass (),
			consoleHelper);

	}

	public synchronized
	ConsoleHelper <?> findByObjectClass (
			@NonNull Class <?> objectClass) {

		return byObjectClass.get (
			objectClass);

	}

	public synchronized
	ConsoleHelper <?> findByObjectName (
			@NonNull String objectName) {

		ConsoleHelper <?> consoleHelper =
			byObjectName.get (
				objectName);

		if (consoleHelper == null) {

			throw new RuntimeException (
				stringFormat (
					"No console helper for %s",
					objectName));

		}

		return consoleHelper;

	}

	public synchronized
	Map<String,ConsoleHelper<?>> byObjectName () {
		return byObjectName;
	}

}
