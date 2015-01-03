package wbs.platform.media.console;

import java.awt.image.BufferedImage;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.media.logic.MediaLogic;
import wbs.platform.media.model.ContentRec;
import wbs.platform.media.model.MediaRec;
import wbs.platform.media.model.MediaTypeRec;

@PrototypeComponent ("mediaImageScaleResponder")
public
class MediaImageScaleResponder
	extends AbstractMediaImageResponder {

	// dependencies

	@Inject
	MediaLogic mediaLogic;

	// implementation

	@Override
	protected
	byte[] getData (
			MediaRec media) {

		int maxWidth =
			requestContext.parameterInt (
				"width");

		int maxHeight =
			requestContext.parameterInt (
				"height");

		MediaTypeRec mediaType =
			media.getMediaType ();

		ContentRec content =
			media.getContent ();

		BufferedImage fullImage =
			mediaLogic.readImage (
				content.getData (),
				mediaType.getMimeType ());

		BufferedImage scaledImage =
			mediaLogic.resampleImage (
				fullImage,
				maxWidth,
				maxHeight);

		byte[] scaledImageData =
			mediaLogic.writeImage (
				scaledImage,
				mediaType.getMimeType ());

		return scaledImageData;

	}

	@Override
	protected
	String getMimeType (
			MediaRec media) {

		MediaTypeRec mediaType =
			media.getMediaType ();

		return mediaType.getMimeType ();

	}

}
