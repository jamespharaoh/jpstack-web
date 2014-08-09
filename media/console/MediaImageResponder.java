package wbs.platform.media.console;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.media.model.MediaRec;

@PrototypeComponent ("mediaImageResponder")
public
class MediaImageResponder
	extends AbstractMediaImageResponder {

	@Override
	protected
	byte[] getData (
			MediaRec media) {

		return media.getContent ().getData ();

	}

	@Override
	protected
	String getMimeType (
			MediaRec media) {

		return media.getMediaType ().getMimeType ();

	}

}
