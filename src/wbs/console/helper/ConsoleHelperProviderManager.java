package wbs.console.helper;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import lombok.extern.log4j.Log4j;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;

@Log4j
@SingletonComponent ("consoleHelperProviderManager")
public
class ConsoleHelperProviderManager {

	// singleton dependencies

	@SingletonDependency
	Map <String, ConsoleHelperProvider <?>>
	consoleHelperProvidersByBeanName;

	// state

	Map <Class <?>, ConsoleHelperProvider <?>> consoleHelperProvidersByClass =
		new HashMap <Class <?>, ConsoleHelperProvider <?>> ();

	// life cycle

	@PostConstruct
	public
	void init () {

		for (
			Map.Entry <String, ConsoleHelperProvider <?>> entry
				: consoleHelperProvidersByBeanName.entrySet ()
		) {

//			String beanName =
//				entry.getKey ();

			ConsoleHelperProvider <?> consoleHelperProvider =
				entry.getValue ();

			// check for dupes

			if (
				consoleHelperProvidersByClass.containsKey (
					consoleHelperProvider.objectClass ())
			) {

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
