package wbs.integrations.clockworksms.foreignapi;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.StrongPrototypeDependency;
import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.data.tools.DataFromXml;
import wbs.framework.data.tools.DataFromXmlBuilder;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@SingletonComponent ("clockworkSmsForeignApiDataFromXml")
public
class ClockworkSmsForeignApiDataFromXml
	implements ComponentFactory <DataFromXml> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@StrongPrototypeDependency
	Provider <DataFromXmlBuilder> dataFromXmlBuilderProvider;

	// components

	@Override
	public
	DataFromXml makeComponent (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"makeComponent");

		) {

			return dataFromXmlBuilderProvider.get ()

				.registerBuilderClasses (
					taskLogger,
					ClockworkSmsMessageResponse.class,
					ClockworkSmsMessageResponse.SmsResp.class)

				.build (
					taskLogger)

			;

		}

	}

}
