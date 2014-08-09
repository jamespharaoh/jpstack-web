package wbs.platform.media.console;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.media.model.MediaRec;

@PrototypeComponent ("mediaThumb32Responder")
public
class MediaThumb32Responder
	extends AbstractMediaImageResponder {

	@Override
	protected
	byte[] getData (
			MediaRec media) {

		return media.getThumb32Content ().getData ();

	}

	@Override
	protected
	String getMimeType (
			MediaRec media) {

		return media.getThumbMediaType ().getMimeType ();

	}

}
