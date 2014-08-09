package wbs.framework.application.config;

import static wbs.framework.utils.etc.Misc.stringFormat;
import wbs.framework.application.annotations.SingletonComponent;

@SingletonComponent ("wbsConfigLoader")
public
class WbsConfigFactory {

	@SingletonComponent ("wbsConfig")
	public
	WbsConfig wbsConfig () {

		String configFilename =
			System.getenv (
				"WBS_CONFIG_XML");

		if (configFilename == null) {

			throw new RuntimeException (
				stringFormat (
					"Please set WBS_CONFIG_XML"));

		}

		WbsConfig wbsConfig =
			WbsConfig.readFilename (
				configFilename);

		return wbsConfig;

	}

}
