package wbs.apn.chat.infosite.api;

import static wbs.framework.utils.etc.Misc.equal;

import java.io.IOException;
import java.io.OutputStream;

import javax.inject.Inject;

import wbs.apn.chat.infosite.model.ChatInfoSiteObjectHelper;
import wbs.apn.chat.infosite.model.ChatInfoSiteRec;
import wbs.apn.chat.user.core.model.ChatUserRec;
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
				requestContext.requestInt ("chatInfoSiteId"));

		if (! equal (
			infoSite.getToken (),
			requestContext.request ("chatInfoSiteToken"))) {

			throw new RuntimeException ("Token mismatch");

		}

		int index =
			requestContext.requestInt ("chatInfoSiteIndex");

		ChatUserRec chatUser =
			infoSite.getOtherChatUsers ().get (index);

		if (chatUser == null)
			throw new RuntimeException ("Index out of bounds");

		String mode = (String)
			requestContext.request ("chatUserSiteMode");

		if (equal (mode, "full")) {

			media =
				chatUser.getMainChatUserImage ().getFullMedia ();

		} else {

			media =
				chatUser.getMainChatUserImage ().getMedia ();

		}

		data = media.getContent ().getData ();

	}

	@Override
	protected void goHeaders () throws IOException {
		requestContext.setHeader ("Content-Type", media.getMediaType ().getMimeType ());
		requestContext.setHeader ("Content-Length", Integer.toString (data.length));
	}

	@Override
	protected void goContent ()
		throws IOException {

		OutputStream out =
			requestContext.outputStream ();

		out.write (data);

	}

}
