package wbs.imchat.api;

import javax.inject.Provider;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.UninitializedDependency;

import wbs.web.action.Action;

@SingletonComponent ("imChatMediaConfig")
public
class ImChatMediaConfig {

	// unitialized dependencies

	@UninitializedDependency
	Provider <ImChatMediaJpegAction> imChatMediaJpegActionProvider;

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
				98l);

	}

	@PrototypeComponent ("imChatMediaMiniatureJpegAction")
	public
	Action imChatMediaMiniatureJpegAction () {

		return imChatMediaJpegActionProvider.get ()

			.targetWidth (
				32l)

			.targetHeight (
				32l);

	}

}
