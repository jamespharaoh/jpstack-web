package wbs.clients.apn.chat.user.image.console;

import java.io.IOException;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.image.model.ChatUserImageRec;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.ConsoleResponder;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.annotations.SingletonDependency;

@PrototypeComponent ("chatUserImageZipResponder")
public
class ChatUserImageZipResponder
	extends ConsoleResponder {

	// singleton dependencies

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// state

	List <ChatUserRec> chatUsers;

	// implementation

	@Override
	@SuppressWarnings ("unchecked")
	public
	void prepare () {

		chatUsers =
			(List<ChatUserRec>)
			requestContext.request ("chatUsers");

	}

	@Override
	public
	void setHtmlHeaders () {

		requestContext.setHeader (
			"Content-Type",
			"application/zip");

		requestContext.setHeader (
			"Content-Disposition",
			"download; filename=photos.zip");

	}

	@Override
	public
	void render ()
		throws IOException {

		ZipOutputStream zipOutputStream =
			new ZipOutputStream (
				requestContext.outputStream ());

		zipOutputStream.setMethod (
			ZipOutputStream.STORED);

		for (
			ChatUserRec chatUser
				: chatUsers
		) {

			ZipEntry zipEntry =
				new ZipEntry (
					chatUser.getCode () + ".jpg");

			ChatUserImageRec chatUserImage =
				chatUser.getChatUserImageList ().get (0);

			byte[] data =
				chatUserImage.getMedia ().getContent ().getData ();

			zipEntry.setSize (
				data.length);

			zipEntry.setCrc (
				getCrc32 (data));

			zipEntry.setTime (
				chatUserImage.getTimestamp ().getMillis ());

			zipOutputStream.putNextEntry (
				zipEntry);

			zipOutputStream.write (
				data);

			zipOutputStream.closeEntry ();

		}

		zipOutputStream.finish ();
		zipOutputStream.close ();

	}

	private static
	long getCrc32 (
			byte[] data) {

		CRC32 crc32 =
			new CRC32 ();

		crc32.update (
			data);

		return crc32.getValue ();

	}

}
