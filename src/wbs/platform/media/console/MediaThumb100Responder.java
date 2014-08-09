package wbs.platform.media.console;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.media.model.MediaRec;

@PrototypeComponent ("mediaThumb100Responder")
public
class MediaThumb100Responder
	extends AbstractMediaImageResponder {

	@Override
	protected
	byte[] getData (
			MediaRec media) {

		return media.getThumb100Content ().getData ();

	}

	@Override
	protected
	String getMimeType (
			MediaRec media) {

		return media.getThumbMediaType ().getMimeType ();

	}

}
