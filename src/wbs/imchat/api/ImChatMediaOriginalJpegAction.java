package wbs.imchat.api;

import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.string.StringUtils.stringNotEqualSafe;

import javax.inject.Provider;

import lombok.Cleanup;
import lombok.NonNull;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.TaskLogger;
import wbs.imchat.model.ImChatObjectHelper;
import wbs.imchat.model.ImChatProfileObjectHelper;
import wbs.platform.media.model.ContentRec;
import wbs.platform.media.model.MediaObjectHelper;
import wbs.platform.media.model.MediaRec;
import wbs.web.action.Action;
import wbs.web.context.RequestContext;
import wbs.web.exceptions.HttpNotFoundException;
import wbs.web.responder.JsonResponder;
import wbs.web.responder.Responder;

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
	Responder handle (
			@NonNull TaskLogger parentTaskLogger) {

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
				() -> new HttpNotFoundException (
					optionalAbsent (),
					emptyList ()));

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

			throw new HttpNotFoundException (
				optionalAbsent (),
				emptyList ());

		}

		// create response

		return imChatMediaResponderProvider.get ()

			.data (
				content.getData ())

			.contentType (
				media.getMediaType ().getMimeType ());

	}

}
