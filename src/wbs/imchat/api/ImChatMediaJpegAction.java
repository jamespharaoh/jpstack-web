package wbs.imchat.api;

import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NumberUtils.maximumJavaInteger;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.string.StringUtils.stringNotEqualSafe;

import java.awt.image.BufferedImage;

import javax.inject.Provider;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.media.logic.MediaLogic;
import wbs.platform.media.model.ContentRec;
import wbs.platform.media.model.MediaObjectHelper;
import wbs.platform.media.model.MediaRec;

import wbs.imchat.model.ImChatObjectHelper;
import wbs.imchat.model.ImChatProfileObjectHelper;
import wbs.web.action.Action;
import wbs.web.context.RequestContext;
import wbs.web.exceptions.HttpNotFoundException;
import wbs.web.responder.JsonResponder;
import wbs.web.responder.Responder;

@PrototypeComponent ("imChatMediaJpegAction")
@Accessors (fluent = true)
public
class ImChatMediaJpegAction
	implements Action {

	// dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	ImChatObjectHelper imChatHelper;

	@SingletonDependency
	ImChatProfileObjectHelper imChatProfileHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MediaObjectHelper mediaHelper;

	@SingletonDependency
	MediaLogic mediaLogic;

	@SingletonDependency
	RequestContext requestContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <ImChatMediaResponder> imChatMediaResponderProvider;

	@PrototypeDependency
	Provider <JsonResponder> jsonResponderProvider;

	// properties

	@Getter @Setter
	Long targetWidth;

	@Getter @Setter
	Long targetHeight;

	// implementation

	@Override
	public
	Responder handle (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"handle ()");

		// begin transaction

		try (

			Transaction transaction =
				database.beginReadOnly (
					taskLogger,
					"ImChatMediaJpegAction.handle ()",
					this);

		) {

			// retrieve media data

			MediaRec media =
				mediaHelper.findOrThrow (
					Long.parseLong (
						requestContext.requestStringRequired (
							"mediaId")),
					() -> new HttpNotFoundException (
						optionalAbsent (),
						emptyList ()));

			// check content hash

			ContentRec content =
				media.getContent ();

			Long hash =
				Math.abs (
					content.getHash ());

			if (
				stringNotEqualSafe (
					hash.toString (),
					requestContext.requestStringRequired (
						"mediaContentHash"))
			) {

				throw new HttpNotFoundException (
					optionalAbsent (),
					emptyList ());

			}

			// resize image

			BufferedImage originalImage =
				mediaLogic.readImageRequired (
					taskLogger,
					content.getData (),
					media.getMediaType ().getMimeType ());

			BufferedImage resizedImage;

			if (

				isNotNull (
					targetWidth)

				&& isNotNull (
					targetHeight)

			) {

				resizedImage =
					mediaLogic.cropAndResampleImage (
						originalImage,
						targetWidth,
						targetHeight);

			} else {

				resizedImage =
					mediaLogic.resampleImageToFit (
						originalImage,
						ifNull (
							targetWidth,
							maximumJavaInteger),
						ifNull (
							targetHeight,
							maximumJavaInteger));

			}

			// create jpeg

			byte[] resizedImageJpeg =
				mediaLogic.writeJpeg (
					resizedImage,
					0.8f);

			// create response

			return imChatMediaResponderProvider.get ()

				.data (
					resizedImageJpeg)

				.contentType (
					"image/jpeg");

		}

	}

}
