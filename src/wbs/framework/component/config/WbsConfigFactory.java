package wbs.framework.component.config;

import static wbs.utils.string.StringUtils.stringFormat;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@SingletonComponent ("wbsConfigLoader")
public
class WbsConfigFactory {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// components

	@SingletonComponent ("wbsConfig")
	public
	WbsConfig wbsConfig (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"wbsConfig");

		) {

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
					taskLogger,
					configFilename);

			return wbsConfig;

		}

	}

}
