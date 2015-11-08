package wbs.applications.imchat.api;

import static wbs.framework.utils.etc.Misc.notEqual;

import java.awt.image.BufferedImage;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;

import wbs.applications.imchat.model.ImChatObjectHelper;
import wbs.applications.imchat.model.ImChatProfileObjectHelper;
import wbs.framework.application.annotations.PrototypeComponent;
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

@PrototypeComponent ("imChatMediaThumbnailJpegAction")
public
class ImChatMediaThumbnailJpegAction
	implements Action {

	// dependencies

	@Inject
	Database database;

	@Inject
	ImChatObjectHelper imChatHelper;

	@Inject
	ImChatProfileObjectHelper imChatProfileHelper;

	@Inject
	MediaObjectHelper mediaHelper;

	@Inject
	MediaLogic mediaLogic;

	@Inject
	RequestContext requestContext;

	// prototype dependencies

	@Inject
	Provider<ImChatMediaResponder> imChatMediaResponderProvider;

	@Inject
	Provider<JsonResponder> jsonResponderProvider;

	// implementation

	@Override
	public
	Responder handle () {

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				this);

		// retrieve media data

		MediaRec media =
			mediaHelper.find (
				Integer.parseInt (
					(String)
					requestContext.request (
						"mediaId")));

		if (media == null) {
			throw new PageNotFoundException ();
		}

		// check content hash

		ContentRec content =
			media.getContent ();

		Integer hash =
			Math.abs (
				content.getHash ());

		if (
			notEqual (
				hash.toString (),
				requestContext.request (
					"mediaContentHash"))
		) {
			throw new PageNotFoundException ();
		}

		// resize image

		BufferedImage originalImage =
			mediaLogic.readImage (
				content.getData (),
				media.getMediaType ().getMimeType ());

		int resizedWidth = 98;

		int resizedHeight =
			media.getHeight () * resizedWidth / media.getWidth ();

		BufferedImage resizedImage =
			mediaLogic.resampleImage (
				originalImage,
				resizedWidth,
				resizedHeight);

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
