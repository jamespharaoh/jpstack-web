package wbs.console.helper;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import lombok.extern.log4j.Log4j;

import wbs.framework.application.annotations.SingletonComponent;

@Log4j
@SingletonComponent ("consoleHelperProviderManager")
public
class ConsoleHelperProviderManager {

	@Inject Map<String,ConsoleHelperProvider<?>>
	consoleHelperProvidersByBeanName;

	Map<Class<?>,ConsoleHelperProvider<?>> consoleHelperProvidersByClass =
		new HashMap<Class<?>,ConsoleHelperProvider<?>> ();

	@PostConstruct
	public
	void init () {

		for (Map.Entry<String,ConsoleHelperProvider<?>> entry
				: consoleHelperProvidersByBeanName.entrySet ()) {

//			String beanName =
//				entry.getKey ();

			ConsoleHelperProvider<?> consoleHelperProvider =
				entry.getValue ();

			// check for dupes

			if (consoleHelperProvidersByClass.containsKey (
					consoleHelperProvider.objectClass ())) {

				log.error (
					stringFormat (
						"Ignoring duplicate helper provider for class %s",
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
