package wbs.applications.imchat.api;

import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.NullUtils.ifNull;
import static wbs.framework.utils.etc.NumberUtils.maximumJavaInteger;
import static wbs.framework.utils.etc.StringUtils.stringNotEqualSafe;

import java.awt.image.BufferedImage;

import javax.inject.Provider;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.applications.imchat.model.ImChatObjectHelper;
import wbs.applications.imchat.model.ImChatProfileObjectHelper;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.annotations.PrototypeDependency;
import wbs.framework.application.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Action;
import wbs.framework.web.JsonResponder;
import wbs.framework.web.PageNotFoundException;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;
import wbs.platform.media.logic.MediaLogic;
import wbs.platform.media.model.ContentRec;
import wbs.platform.media.model.MediaObjectHelper;
import wbs.platform.media.model.MediaRec;

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
	Responder handle () {

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				"ImChatMediaJpegAction.handle ()",
				this);

		// retrieve media data

		MediaRec media =
			mediaHelper.findOrThrow (
				Long.parseLong (
					requestContext.requestStringRequired (
						"mediaId")),
				() -> new PageNotFoundException ());

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
			throw new PageNotFoundException ();
		}

		// resize image

		BufferedImage originalImage =
			mediaLogic.readImageRequired (
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
