package wbs.imchat.core.api;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Action;
import wbs.framework.web.JsonResponder;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;
import wbs.imchat.core.model.ImChatObjectHelper;
import wbs.imchat.core.model.ImChatProfileObjectHelper;
import wbs.platform.media.model.ContentRec;
import wbs.platform.media.model.MediaObjectHelper;
import wbs.platform.media.model.MediaRec;

@PrototypeComponent ("imChatMediaOriginalJpegAction")
public 
class ImChatMediaOriginalJpegAction 
	implements Action {
	
	// dependencies
	
	@Inject
	Database database;
	
	@Inject
	ImChatObjectHelper imChatHelper;
	
	@Inject
	MediaObjectHelper mediaHelper;
	
	@Inject
	ImChatProfileObjectHelper imChatProfileHelper;
	
	@Inject
	RequestContext requestContext;
	
	// prototype dependencies
	
	@Inject
	Provider<JsonResponder> jsonResponderProvider;
	
	// implementation
	
	@Override
	public
	Responder handle () {
	
		// begin transaction
	
		@Cleanup
		Transaction transaction =
			database.beginReadOnly ();
	
		// retrieve media data
		
		MediaRec media =
			mediaHelper.find (
				Integer.parseInt (
					(String)
					requestContext.request (
						"mediaId")));
		
		// check content hash
		
		ContentRec content =
			media.getContent();
		
		Integer hash = 
			Math.abs(content.getHash());
		
		if (
				!hash.toString()
					.equals(requestContext.request (
						"mediaContentHash"))
			) {

				ImChatFailure failureResponse =
					new ImChatFailure ()

					.reason (
						"content hash does not match")

					.message (
						"The specified content hash is invalid.");

				return jsonResponderProvider.get ()
					.value (failureResponse);

			}
		
		// create response
	
		ImChatMediaOriginalJpegSuccess successResponse =
				new ImChatMediaOriginalJpegSuccess ()

				.data (
					content.getData())

				.mimeType (
					media.getMediaType()
						.getMimeType());
		
		return jsonResponderProvider.get ()
			.value (successResponse);
	
	}

}
