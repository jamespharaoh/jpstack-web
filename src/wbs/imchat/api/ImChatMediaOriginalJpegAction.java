package wbs.imchat.api;

import static wbs.utils.string.StringUtils.stringNotEqualSafe;

import javax.inject.Provider;

import lombok.Cleanup;
import wbs.imchat.model.ImChatObjectHelper;
import wbs.imchat.model.ImChatProfileObjectHelper;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Action;
import wbs.framework.web.JsonResponder;
import wbs.framework.web.PageNotFoundException;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;
import wbs.platform.media.model.ContentRec;
import wbs.platform.media.model.MediaObjectHelper;
import wbs.platform.media.model.MediaRec;

@PrototypeComponent ("imChatMediaOriginalJpegAction")
public
class ImChatMediaOriginalJpegAction
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
	RequestContext requestContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <ImChatMediaResponder> imChatMediaResponderProvider;

	@PrototypeDependency
	Provider <JsonResponder> jsonResponderProvider;

	// implementation

	@Override
	public
	Responder handle () {

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				"ImChatMediaOriginalJpecAction.handle ()",
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

		// create response

		return imChatMediaResponderProvider.get ()

			.data (
				content.getData ())

			.contentType (
				media.getMediaType ().getMimeType ());

	}

}
