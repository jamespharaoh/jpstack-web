package wbs.integrations.clockworksms.api;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.UninitializedDependency;
import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.data.tools.DataFromXml;
import wbs.framework.data.tools.DataFromXmlBuilder;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@SingletonComponent ("clockworkSmsRouteReportRequestReader")
public
class ClockworkSmsRouteReportRequestReader
	implements ComponentFactory <DataFromXml> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// uninitialised dependencies

	@UninitializedDependency
	Provider <DataFromXmlBuilder> dataFromXmlBuilderProvider;

	// implementation

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
					ClockworkSmsRouteReportRequest.class,
					ClockworkSmsRouteReportRequest.Item.class)

				.build (
					taskLogger)

			;

		}

	}

}
