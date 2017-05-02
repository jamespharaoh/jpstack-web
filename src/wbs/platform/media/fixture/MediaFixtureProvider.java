package wbs.platform.media.fixture;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.logging.LogContext;

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
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createFixtures");

		) {

			createTextMediaTypes (
				transaction);

			createImageMediaTypes (
				transaction);

			createVideoMediaTypes (
				transaction);

		}

	}

	private
	void createTextMediaTypes (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createTextMediaTypes");

		) {

			createMediaType (
				transaction,
				"text/plain",
				"Plain text",
				"txt");

		}

	}

	private
	void createImageMediaTypes (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createImageMediaTypes");

		) {

			createMediaType (
				transaction,
				"image/jpeg",
				"JPEG image",
				"jpg");

			createMediaType (
				transaction,
				"image/gif",
				"GIF image",
				"git");

			createMediaType (
				transaction,
				"image/png",
				"PNG image",
				"png");

			createMediaType ( // TODO surely this is not right?!?
				transaction,
				"image/mp4",
				"MPEG-4 image",
				"mp4");

		}

	}

	private
	void createVideoMediaTypes (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createVideoMediaTypes");

		) {

			createMediaType (
				transaction,
				"video/3gpp",
				"3GPP video",
				"3gp");

			createMediaType (
				transaction,
				"video/mpeg",
				"MPEG video",
				"3gp");

		}

	}

	private
	void createMediaType (
			@NonNull Transaction parentTransaction,
			@NonNull String mimeType,
			@NonNull String description,
			@NonNull String extension) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createMediaType");

		) {

			mediaTypeHelper.insert (
				transaction,
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

}
