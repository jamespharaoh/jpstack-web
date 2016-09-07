package wbs.platform.media.fixture;

import javax.inject.Inject;

import lombok.NonNull;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.platform.media.model.MediaTypeObjectHelper;

@PrototypeComponent ("mediaFixtureProvider")
public
class MediaFixtureProvider
	implements FixtureProvider {

	// dependencies

	@Inject
	MediaTypeObjectHelper mediaTypeHelper;

	// implementation

	@Override
	public
	void createFixtures () {

		createTextMediaTypes ();
		createImageMediaTypes ();
		createVideoMediaTypes ();

	}

	private
	void createTextMediaTypes () {

		createMediaType (
			"text/plain",
			"Plain text",
			"txt");

	}

	private
	void createImageMediaTypes () {

		createMediaType (
			"image/jpeg",
			"JPEG image",
			"jpg");

		createMediaType (
			"image/gif",
			"GIF image",
			"git");

		createMediaType (
			"image/png",
			"PNG image",
			"png");

		createMediaType ( // TODO surely this is not right?!?
			"image/mp4",
			"MPEG-4 image",
			"mp4");

	}

	private
	void createVideoMediaTypes () {

		createMediaType (
			"video/3gpp",
			"3GPP video",
			"3gp");

		createMediaType (
			"video/mpeg",
			"MPEG video",
			"3gp");

	}

	private
	void createMediaType (
			@NonNull String mimeType,
			@NonNull String description,
			@NonNull String extension) {

		mediaTypeHelper.insert (
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
