package wbs.apn.chat.user.image.console;

import static wbs.framework.utils.etc.Misc.toEnum;
import wbs.apn.chat.user.image.model.ChatUserImageType;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.part.AbstractPagePart;

@PrototypeComponent ("chatUserImageUploadPart")
public
class ChatUserImageUploadPart
	extends AbstractPagePart {

	ChatUserImageType chatUserImageType;

	@Override
	public
	void prepare () {

		chatUserImageType =
			toEnum (
				ChatUserImageType.class,
				(String) requestContext.stuff ("chatUserImageType"));

	}

	@Override
	public
	void goBodyStuff () {

		printFormat (
			"<p>Please upload the photo or video.</p>\n");

		printFormat (
			"<form",
			" method=\"post\"",
			" enctype=\"multipart/form-data\"",
			">\n");

		printFormat (
			"<p>File to upload<br>\n",
			"<input",
			" type=\"file\"",
			" name=\"upload\"",
			"></p>\n");

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" value=\"upload file\"",
			"></p>\n");

		printFormat (
			"</form>\n");

	}

}
