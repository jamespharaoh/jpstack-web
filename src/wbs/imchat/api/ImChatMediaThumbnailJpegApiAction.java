package wbs.imchat.api;

import lombok.NonNull;

import wbs.api.mvc.ApiAction;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.StrongPrototypeDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("imChatMediaThumbnailJpegApiAction")
public
class ImChatMediaThumbnailJpegApiAction
	implements ComponentFactory <ApiAction> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@StrongPrototypeDependency
	ComponentProvider <ImChatMediaJpegApiAction>
		imChatMediaJpegApiActionProvider;

	// implementation

	@Override
	public
	ApiAction makeComponent (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"makeComponent");

		) {

			return imChatMediaJpegApiActionProvider.provide (
				taskLogger,
				imChatMediaJpegApiAction ->
					imChatMediaJpegApiAction

				.targetWidth (
					98l)

			);

		}

	}

}
