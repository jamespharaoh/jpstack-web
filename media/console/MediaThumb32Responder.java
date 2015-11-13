package wbs.platform.media.console;

import lombok.NonNull;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.media.model.MediaRec;

@PrototypeComponent ("mediaThumb32Responder")
public
class MediaThumb32Responder
	extends AbstractMediaImageResponder {

	@Override
	protected
	byte[] getData (
			@NonNull MediaRec media) {

		return media.getThumb32Content ().getData ();

	}

	@Override
	protected
	String getMimeType (
			@NonNull MediaRec media) {

		return media.getThumbMediaType ().getMimeType ();

	}

}
