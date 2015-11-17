package wbs.applications.imchat.api;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.web.Action;

@SingletonComponent ("imChatMediaConfig")
public
class ImChatMediaConfig {

	// prototype dependencies

	@Inject
	Provider<ImChatMediaJpegAction> imChatMediaJpegActionProvider;

	// implementation

	@PrototypeComponent ("imChatMediaOriginalJpegAction")
	public
	Action imChatMediaOriginalJpegAction () {

		return imChatMediaJpegActionProvider.get ();

	}

	@PrototypeComponent ("imChatMediaThumbnailJpegAction")
	public
	Action imChatMediaThumbnailJpegAction () {

		return imChatMediaJpegActionProvider.get ()

			.targetWidth (
				98);

	}

	@PrototypeComponent ("imChatMediaMiniatureJpegAction")
	public
	Action imChatMediaMiniatureJpegAction () {

		return imChatMediaJpegActionProvider.get ()

			.targetWidth (
				24)

			.targetHeight (
				24);

	}

}
