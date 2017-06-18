package wbs.integrations.oxygenate.api;

import static wbs.utils.etc.OptionalUtils.optionalCast;

import java.io.InputStream;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.StrongPrototypeDependency;
import wbs.framework.data.tools.DataFromXml;
import wbs.framework.data.tools.DataFromXmlBuilder;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@SingletonComponent ("oxygenateRouteInMmsRequestBuilder")
public
class OxygenateRouteInMmsNewRequestBuilder {

	// singleton components

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@StrongPrototypeDependency
	Provider <DataFromXmlBuilder> dataFromXmlBuilderProvider;

	// state

	DataFromXml dataFromXml;

	// life cycle

	@NormalLifecycleSetup
	public
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setup");

		) {

			dataFromXml =
				dataFromXmlBuilderProvider.get ()

				.registerBuilderClasses (
					taskLogger,
					OxygenateRouteInMmsNewRequest.class,
					OxygenateRouteInMmsNewRequest.Attachment.class)

				.build (
					taskLogger)

			;

		}

	}

	// public implementation

	public
	Optional <OxygenateRouteInMmsNewRequest> readInputStream (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull InputStream inputStream) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"build");

		) {

			return optionalCast (
				OxygenateRouteInMmsNewRequest.class,
				dataFromXml.readInputStream (
					taskLogger,
					inputStream,
					"oxygen8-route-in-mms-new.xml"));

		}

	}

}
