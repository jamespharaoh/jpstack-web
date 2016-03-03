package wbs.clients.apn.chat.infosite.api;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.notEqual;

import java.io.IOException;
import java.io.OutputStream;

import javax.inject.Inject;

import wbs.clients.apn.chat.infosite.model.ChatInfoSiteObjectHelper;
import wbs.clients.apn.chat.infosite.model.ChatInfoSiteRec;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.web.AbstractResponder;
import wbs.framework.web.RequestContext;
import wbs.platform.media.model.MediaRec;

@PrototypeComponent ("chatInfoSiteImageResponder")
public
class ChatInfoSiteImageResponder
	extends AbstractResponder {

	// dependencies

	@Inject
	ChatInfoSiteObjectHelper chatInfoSiteHelper;

	@Inject
	RequestContext requestContext;

	// state

	ChatInfoSiteRec infoSite;
	MediaRec media;
	byte[] data;

	// implementation

	@Override
	protected
	void prepare () {

		infoSite =
			chatInfoSiteHelper.find (
				requestContext.requestIntRequired (
					"chatInfoSiteId"));

		if (
			notEqual (
				infoSite.getToken (),
				requestContext.requestStringRequired (
					"chatInfoSiteToken"))
		) {

			throw new RuntimeException (
				"Token mismatch");

		}

		int index =
			requestContext.requestIntRequired (
				"chatInfoSiteIndex");

		ChatUserRec chatUser =
			infoSite.getOtherChatUsers ().get (
				index);

		if (chatUser == null) {

			throw new RuntimeException (
				"Index out of bounds");

		}

		String mode =
			requestContext.requestStringRequired (
				"chatInfoSiteMode");

		if (
			equal (
				mode,
				"full")
		) {

			media =
				chatUser.getMainChatUserImage ().getFullMedia ();

		} else {

			media =
				chatUser.getMainChatUserImage ().getMedia ();

		}

		data =
			media.getContent ().getData ();

	}

	@Override
	protected
	void goHeaders () {

		requestContext.setHeader (
			"Content-Type",
			media.getMediaType ().getMimeType ());

		requestContext.setHeader (
			"Content-Length",
			Integer.toString (
				data.length));

	}

	@Override
	protected
	void goContent ()
		throws IOException {

		OutputStream out =
			requestContext.outputStream ();

		out.write (
			data);

	}

}
