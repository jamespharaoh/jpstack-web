package wbs.platform.media.fixture;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.media.model.MediaTypeObjectHelper;

@PrototypeComponent ("mediaFixtureProvider")
public
class MediaFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MediaTypeObjectHelper mediaTypeHelper;

	// implementation

	@Override
	public
	void createFixtures (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"createFixtures");

		createTextMediaTypes (
			taskLogger);

		createImageMediaTypes (
			taskLogger);

		createVideoMediaTypes (
			taskLogger);

	}

	private
	void createTextMediaTypes (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"createTextMediaTypes");

		createMediaType (
			taskLogger,
			"text/plain",
			"Plain text",
			"txt");

	}

	private
	void createImageMediaTypes (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"createImageMediaTypes");

		createMediaType (
			taskLogger,
			"image/jpeg",
			"JPEG image",
			"jpg");

		createMediaType (
			taskLogger,
			"image/gif",
			"GIF image",
			"git");

		createMediaType (
			taskLogger,
			"image/png",
			"PNG image",
			"png");

		createMediaType ( // TODO surely this is not right?!?
			taskLogger,
			"image/mp4",
			"MPEG-4 image",
			"mp4");

	}

	private
	void createVideoMediaTypes (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"createVideoMediaTypes");

		createMediaType (
			taskLogger,
			"video/3gpp",
			"3GPP video",
			"3gp");

		createMediaType (
			taskLogger,
			"video/mpeg",
			"MPEG video",
			"3gp");

	}

	private
	void createMediaType (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String mimeType,
			@NonNull String description,
			@NonNull String extension) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"createMediaType");

		mediaTypeHelper.insert (
			taskLogger,
			mediaTypeHelper.createInstance ()

			.setMimeType (
				mimeType)

			.setDescription (
				description)

			.setExtension (
				extension)

		);

	}

}
