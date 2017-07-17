package wbs.integrations.clockworksms.api;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.manager.ComponentProvider;
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

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <DataFromXmlBuilder> dataFromXmlBuilderProvider;

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

			return dataFromXmlBuilderProvider.provide (
				taskLogger)

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
